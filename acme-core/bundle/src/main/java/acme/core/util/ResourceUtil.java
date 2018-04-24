package acme.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;

public final class ResourceUtil {

    public static boolean isExistingResource(@Nonnull ResourceResolver resourceResolver, String resourceAbsolutePath) {
        return StringUtils.isNotBlank(resourceAbsolutePath)
                && isAbsoluteResourcePath(resourceAbsolutePath)
                && !org.apache.sling.api.resource.ResourceUtil.isNonExistingResource(resourceResolver.resolve(resourceAbsolutePath));
    }

    public static boolean isAbsoluteResourcePath(String path) {
        return StringUtils.startsWith(path, Constants.Url.SLASH);
    }

    private ResourceUtil() {
        throw new UnsupportedOperationException(Constants.E_NO_INSTANCE);
    }

}
