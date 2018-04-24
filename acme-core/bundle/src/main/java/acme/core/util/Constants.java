package acme.core.util;

public final class Constants {

    public static final String E_NO_INSTANCE = "Utility class class cannot be instantiated";

    public static final class ServiceUsers {

        public static final String CONTENT_READER_SERVICE = "content-reader-service";

        private ServiceUsers() {
            throw new UnsupportedOperationException(E_NO_INSTANCE);
        }
    }

    public static final class PropertyNames {

        public static final String JCR_CONTENT = "jcr:content";

        private PropertyNames() {
            throw new UnsupportedOperationException(E_NO_INSTANCE);
        }
    }

    public static final class Url {

        public static final String DOT_HTML = ".html";

        public static final String SLASH = "/";

        public static final String CONTENT_ROOT = "/content";

        public static final String CONTENT_PATH = CONTENT_ROOT + "/";

        private Url() {
            throw new UnsupportedOperationException(E_NO_INSTANCE);
        }

    }

    private Constants() {
        throw new UnsupportedOperationException(E_NO_INSTANCE);
    }

}
