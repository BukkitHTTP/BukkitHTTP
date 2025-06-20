package nano.http.d2.qr;


/**
 * Thrown when the supplied data does not fit any QR Code version. Ways to handle this exception include:
 * <ul>
 *   <li><p>Decrease the error correction level if it was greater than {@code Ecc.LOW}.</p></li>
 *   <li><p>If the advanced {@code encodeSegments()} function with 6 arguments or the
 *     {@code makeSegmentsOptimally()} function was called, then increase the maxVersion argument
 *     if it was less than {@link QrCode#MAX_VERSION}. (This advice does not apply to the other
 *     factory functions because they search all versions up to {@code QrCode.MAX_VERSION}.)</p></li>
 *   <li><p>Split the text data into better or optimal segments in order to reduce the number of
 *     bits required. (See {@link QrSegmentAdvanced#makeSegmentsOptimally(CharSequence, QrCode.Ecc, int, int)
 *     QrSegmentAdvanced.makeSegmentsOptimally()}.)</p></li>
 *   <li><p>Change the text or binary data to be shorter.</p></li>
 *   <li><p>Change the text to fit the character set of a particular segment mode (e.g. alphanumeric).</p></li>
 *   <li><p>Propagate the error upward to the caller/user.</p></li>
 * </ul>
 *
 * @see QrCode#encodeText(CharSequence, QrCode.Ecc)
 * @see QrCode#encodeBinary(byte[], QrCode.Ecc)
 * @see QrCode#encodeSegments(java.util.List, QrCode.Ecc)
 * @see QrCode#encodeSegments(java.util.List, QrCode.Ecc, int, int, int, boolean)
 * @see QrSegmentAdvanced#makeSegmentsOptimally(CharSequence, QrCode.Ecc, int, int)
 */
public class DataTooLongException extends IllegalArgumentException {

    public DataTooLongException() {
    }


    public DataTooLongException(String msg) {
        super(msg);
    }

}
