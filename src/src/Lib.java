import java.util.Base64;

public class Lib {
    public static String decode_from_base64(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded);
    }
}


