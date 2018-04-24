package acme.core.services.slingrequest;

import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public interface SlingRequestService {

    SlingResponse doGet(String targetResourcePath, @Nonnull ResourceResolver resourceResolver);

    SlingResponse doGet(HttpServletRequest request, @Nonnull ResourceResolver resourceResolver);

    HttpServletRequest createRequest(String targetResourcePath);
}
