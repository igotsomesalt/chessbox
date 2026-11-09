package chessbox.util;

import java.util.NoSuchElementException;
import java.util.Set;

public class UUID {

    private UUID() {}

    public static String shortId(java.util.UUID id) {
        return id.toString().replace("-", "").substring(0, 7);
    }

    public static java.util.UUID findKey(String shortId, Set<java.util.UUID> keySet) throws IllegalArgumentException, NoSuchElementException {
        String prefix = shortId.toLowerCase();
        java.util.UUID match = null;

        for (java.util.UUID uuid : keySet) {
            if (uuid.toString().startsWith(prefix)) {
                if (match != null) {
                    throw new IllegalArgumentException(
                        "Ambiguous ID: " + shortId
                    );
                }

                match = uuid;
            }
        }

        if (match == null) {
            throw new NoSuchElementException(
                "Key not found: " + shortId
            );
        }

        return match;
    }
}
