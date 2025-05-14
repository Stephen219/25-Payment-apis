package Kariuki.mpesa.api.utils;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncoderUtil {

    public String convertToBase64(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(data);
    }
}
