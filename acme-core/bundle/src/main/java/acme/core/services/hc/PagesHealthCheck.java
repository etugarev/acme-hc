package acme.core.services.hc;

import acme.core.services.slingrequest.SlingRequestService;
import acme.core.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.api.ResultLog;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Component
@Designate(ocd = PagesHealthCheck.Configuration.class)
@Slf4j
public class PagesHealthCheck implements HealthCheck {

    private static final long TIMEOUT = 30L;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private SlingRequestService requestService;

    private Configuration configuration;

    @ObjectClassDefinition(name = "ACME Pages Health Check Configuration",
            description = "ACME Pages Health Check Service Configuration")
    public @interface Configuration {

        @AttributeDefinition(name = "Name", type = AttributeType.STRING, description = "Name")
        String hc_name() default "ACME Pages";

        @AttributeDefinition(name = "Tags", type = AttributeType.STRING, description = "Tags")
        String hc_tags() default "acme";

        @AttributeDefinition(name = "MBean",
                type = AttributeType.STRING,
                description = "Mbean name (leave empty for not using JMX)")
        String hc_mbean_name() default "acmePagesHealthCheck";

        @AttributeDefinition(name = "Cron expression",
                type = AttributeType.STRING,
                description = "Cron expression for asynchronous execution (leave empty for synchronous execution)")
        String hc_async_cronExpression() default StringUtils.EMPTY;

        @AttributeDefinition(name = "Checked paths",
                description = "Paths where to look for pages.",
                type = AttributeType.STRING)
        String[] hcCheckedRoots() default {};

        @AttributeDefinition(name = "Known error messages",
                description = "A page containing a text from this list will be considered invalid.",
                type = AttributeType.STRING)
        String[] hcKnownErrorMessages() default {};

    }

    @Override
    public Result execute() {
        if (ArrayUtils.isEmpty(configuration.hcCheckedRoots())) {
            return new Result(Result.Status.INFO, "Please configure checked paths.");
        }

        if (ArrayUtils.isEmpty(configuration.hcKnownErrorMessages())) {
            return new Result(Result.Status.INFO, "Please configure known error messages.");
        }

        ResultLog resultLog = new ResultLog();

        return Templates.serviceResourceResolverFunction(Constants.ServiceUsers.CONTENT_READER_SERVICE, resourceResolverFactory, resourceResolver -> {
            for (String path : ListUtil.transformArrayToList(configuration.hcCheckedRoots())) {
                Node currentNode = Optional.of(resourceResolver.resolve(path))
                                           .map(r -> r.adaptTo(Node.class))
                                           .orElse(null);
                if (currentNode == null) {
                    String message = String.format("Can't adapt resource path [%s] to node class, is path valid?", path);
                    return new Result(Result.Status.HEALTH_CHECK_ERROR, message);
                }

                try {
                    collectErrors(currentNode, resultLog);
                } catch (RepositoryException e) {
                    return new Result(Result.Status.HEALTH_CHECK_ERROR, "Oops..", e);
                }
            }
            List<ResultLog.Entry> errorList = IteratorUtils.toList(resultLog.iterator());
            if (errorList.isEmpty()) {
                log.debug("Success, no problems found");
                return new Result(Result.Status.OK, "No problems found.");
            } else {
                log.debug("Found problems: {}", errorList.size());
            }
            return new Result(resultLog);
        });
    }

    @Activate
    @Modified
    public void activate(Configuration configuration) {
        this.configuration = configuration;
    }

    private void collectErrors(final Node currentNode, ResultLog resultLog) throws RepositoryException {
        ExecutorService executor = Executors.newWorkStealingPool();
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        List<Future<Callable<String>>> futures = new ArrayList<>();

        String statement = String.format("SELECT * FROM [cq:Page] AS s WHERE ISSAMENODE([%s]) OR ISDESCENDANTNODE([%s])",
                currentNode.getPath(), currentNode.getPath());
        Templates.executeJcrQueryConsumer(currentNode, statement, Query.JCR_SQL2, queryResult -> {
            List<Node> nodes = IteratorUtils.toList(queryResult.getNodes());
            for (Node node : nodes) {
                Future f = completionService.submit(testResultFunction(node.getPath()));
                futures.add(f);
            }
        });

        int completed = 0;
        boolean isError = false;

        // wait for all tasks to complete before continuing
        while (completed < futures.size() && !isError) {
            try {
                String errorMessage = completionService.take().get(TIMEOUT, TimeUnit.SECONDS);
                completed++;
                if (StringUtils.isNotBlank(errorMessage)) {
                    resultLog.add(new ResultLog.Entry(Result.Status.CRITICAL, errorMessage));
                    log.error("{}", errorMessage);
                }
                log.debug("Total checks: {}, Completed: {}", futures.size(), completed);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                isError = true;
                log.error("Error while executing health check.", e);
            }
        }
        executor.shutdownNow();
    }

    private Callable<String> testResultFunction(final String nodePath) {
        return () -> Templates.serviceResourceResolverFunction(Constants.ServiceUsers.CONTENT_READER_SERVICE,
                resourceResolverFactory, resourceResolver -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Using session {}", resourceResolver.adaptTo(Session.class));
                    }
                    String checkedPageUrl = PageUtil.smartAddDotHtml(nodePath + ".healthcheck") + "/healthcheck";
                    // check errors in html output
                    String result = requestService.doGet(checkedPageUrl, resourceResolver).getContent();
                    for (String knownError : configuration.hcKnownErrorMessages()) {
                        boolean success = result != null && !result.contains(knownError);
                        if (!success) {
                            return String.format("Found [%s] on a page [%s]", knownError, checkedPageUrl);
                        }
                    }
                    // check jcr:content node is present
                    if (!ResourceUtil.isExistingResource(resourceResolver,
                            nodePath + "/" + Constants.PropertyNames.JCR_CONTENT)) {
                        return String.format("Page [%s] is broken, 'jcr:content' child node not found", nodePath);
                    }
                    return StringUtils.EMPTY;
                });
    }

}
