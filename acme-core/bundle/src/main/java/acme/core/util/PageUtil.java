package acme.core.util;

import org.apache.commons.lang3.StringUtils;

public final class PageUtil {

    /**
     * Adds .html to the given URL/link if not yet present and if if the url does not end with /.
     *
     * @param url the url
     * @return url with .html
     */
    public static String smartAddDotHtml(final String url) {
        /*
         * 1. url == null or url.isEmpty()
         * 2. url beginnt nicht mit / (externer link)
         * 3. url.endsWith(".html")
         *    url.contains(".html?")
         *    url.contains(".html#")
         * 4. url.endsWith("/")
         *    e.g. url.endsWith("/?test")
         *    e.g. url.endsWith("/#test")
         */
        if (StringUtils.isEmpty(url)
                || StringUtils.contains(url, Constants.Url.DOT_HTML)
                || !isInternalContentPath(url)
                || url.matches(".+\\.html(|\\?.*|#.*)$")
                || url.matches(".+/(([#][^#/?]*)?|([?][^#/?]*))$")) {
            return url;
        } else {
            return url + Constants.Url.DOT_HTML;
        }
    }

    public static boolean isInternalContentPath(String url) {
        return StringUtils.startsWith(url, Constants.Url.CONTENT_PATH);
    }

    /**
     * Private Constructor.
     */
    private PageUtil() {
        throw new UnsupportedOperationException(Constants.E_NO_INSTANCE);
    }
}
