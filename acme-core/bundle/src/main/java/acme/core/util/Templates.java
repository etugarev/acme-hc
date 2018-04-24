package acme.core.util;

import acme.core.functions.RepositoryConsumer;
import acme.core.functions.RepositoryFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Templates class contains reusable code blocks
 */
@Slf4j
public final class Templates {

    /**
     * Template for using service resource resolver.
     *
     * @param serviceName             service name for which template is invoked
     * @param resourceResolverFactory resource resolver factory
     * @param function                function which uses resource resolver and returns a result of <R> type
     * @param <R>                     function parameter type
     * @return result of <R> type
     */
    public static <R> R serviceResourceResolverFunction(String serviceName,
                                                        @Nonnull ResourceResolverFactory resourceResolverFactory,
                                                        @Nonnull Function<ResourceResolver, R> function) {

        Map<String, Object> map = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, serviceName);
        ResourceResolver resolver = null;
        try {
            resolver = resourceResolverFactory.getServiceResourceResolver(map);
            if (log.isTraceEnabled()) {
                log.trace("Acquiring session {}", resolver.adaptTo(Session.class));
            }
            return function.apply(resolver);
        } catch (LoginException e) {
            log.error("Access denied", e);
            return null;
        } finally {
            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
        }
    }

    public static <R> R executeJcrQueryFunction(@Nonnull Node currentNode, @Nonnull String queryStatement,
                                                @Nonnull String queryLanguage, RepositoryFunction<QueryResult, R> queryFunction) throws RepositoryException {
        return executeJcrQueryFunction(currentNode.getSession(), queryStatement, queryLanguage, queryFunction);
    }

    public static <R> R executeJcrQueryFunction(@Nonnull Session session, @Nonnull String queryStatement,
                                                @Nonnull String queryLanguage, RepositoryFunction<QueryResult, R> queryFunction) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = queryManager.createQuery(queryStatement, queryLanguage).execute();
        return queryFunction.apply(result);
    }

    public static void executeJcrQueryConsumer(@Nonnull Node currentNode, @Nonnull String queryStatement,
                                               @Nonnull String queryLanguage, RepositoryConsumer<QueryResult> queryConsumer) throws RepositoryException {
        executeJcrQueryFunction(currentNode, queryStatement, queryLanguage, queryResult -> {
            queryConsumer.accept(queryResult);
            return null;
        });
    }

    /**
     * Private Constructor.
     */
    private Templates() {
        throw new UnsupportedOperationException(Constants.E_NO_INSTANCE);
    }
}
