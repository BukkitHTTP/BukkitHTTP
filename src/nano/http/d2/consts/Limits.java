package nano.http.d2.consts;

/**
 * Central place for size limits of untrusted input.
 * Tune these instead of scattering magic numbers around.
 */
public final class Limits {
    private Limits() {
    }

    /**
     * Max raw (possibly gzipped) request body size in bytes.
     * (Kept from the original hardcoded 100_000_000.)
     */
    public static final int MAX_BODY_BYTES = 100_000_000;

    /**
     * Max decompressed size of a gzipped request body in bytes.
     * Content-Length only bounds the compressed size, so without this
     * a small zip-bomb would decompress into unbounded memory.
     */
    public static final int MAX_INFLATED_BODY_BYTES = 100_000_000;

    /**
     * Max total size of one (possibly fragmented) WebSocket message in bytes.
     * A single frame is already capped at 2MB by WebSocketMachine; this caps
     * the sum of continuation frames, which used to be unbounded.
     */
    public static final int MAX_WS_MESSAGE_BYTES = 10 * 1024 * 1024;
}
