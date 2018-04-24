package acme.core.services.slingrequest;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public class SlingResponse {

    private static final String DOCTYPE_HTML = "<!doctype html";

    @Getter
    private String content;

    @Getter
    private boolean isSucceeded;

    @Getter
    private boolean isHtmlPage;

    private static SlingResponse failureInstance;

    public SlingResponse(final String content, @Nonnull final HttpServletRequest request) {
        this.content = StringUtils.trim(content);
        isHtmlPage = StringUtils.startsWithIgnoreCase(this.content, DOCTYPE_HTML);
    }

    public static synchronized SlingResponse failure() {
        if (failureInstance == null) {
            failureInstance = new SlingResponse(StringUtils.EMPTY, false);
        }
        return failureInstance;
    }

    private SlingResponse(final String content, final boolean isSucceeded) {
        this.content = content;
        this.isSucceeded = isSucceeded;
    }
}
