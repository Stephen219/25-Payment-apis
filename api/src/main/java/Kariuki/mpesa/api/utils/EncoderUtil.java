package Kariuki.mpesa.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncoderUtil {

    public String convertToBase64(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(data);
    }
    public  String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}
