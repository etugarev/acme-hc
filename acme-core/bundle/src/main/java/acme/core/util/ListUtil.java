package acme.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ListUtil {

    public static <T> List<T> transformArrayToList(T[] array) {
        if (array != null) {
            return Arrays.asList(array);
        }
        return Collections.emptyList();
    }

    /**
     * Private Constructor.
     */
    private ListUtil() {
        throw new UnsupportedOperationException(Constants.E_NO_INSTANCE);
    }
}