package acme.core.services.slingrequest.impl;

import acme.core.services.slingrequest.SlingRequestService;
import acme.core.services.slingrequest.SlingResponse;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component(service = SlingRequestService.class, immediate = true)
public class SlingRequestServiceImpl implements SlingRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlingRequestServiceImpl.class);

    @Reference
    private SlingRequestProcessor requestProcessor;

    @Reference
    private RequestResponseFactory requestResponseFactory;

    @Override
    public SlingResponse doGet(final String targetResourcePath, @Nonnull final ResourceResolver resourceResolver) {
        HttpServletRequest request = createRequest(targetResourcePath);
        return getSlingResponse(request, resourceResolver);
    }

    @Override
    public SlingResponse doGet(HttpServletRequest request, @Nonnull final ResourceResolver resourceResolver) {
        return getSlingResponse(request, resourceResolver);
    }

    @Override
    public HttpServletRequest createRequest(String targetResourcePath) {
        return requestResponseFactory.createRequest("GET", targetResourcePath);
    }

    private SlingResponse getSlingResponse(HttpServletRequest request, ResourceResolver resourceResolver) {
        try {
            WCMMode.DISABLED.toRequest(request);
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpServletResponse response = requestResponseFactory.createResponse(out);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            requestProcessor.processRequest(request, response, resourceResolver);
            return new SlingResponse(out.toString(), request);
        } catch (ServletException | IOException e) {
            LOGGER.error("Can't process internal sling request", e);
        }
        return SlingResponse.failure();
    }
}
