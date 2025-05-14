package Kariuki.mpesa.api.accessToken;

import Kariuki.mpesa.api.config.MpesaConfig;
import Kariuki.mpesa.api.utils.EncoderUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j

public class TokenServiceImpl implements TokenService {

    private final OkHttpClient client;
    private final MpesaConfig config;
    private final ObjectMapper mapper;

    EncoderUtil encoder = new EncoderUtil();
    public static final String BASIC_AUTH_STRING = "Basic";
    public static final String AUTHORIZATION_HEADER_STRING = "authorization";
    public static final String CACHE_CONTROL_HEADER = "cache-control";
    public static final String CACHE_CONTROL_HEADER_VALUE = "no-cache";

    public TokenServiceImpl(OkHttpClient client, MpesaConfig config, ObjectMapper mapper) {
        this.client = client;
        this.config = config;
        this.mapper = mapper;
    }


    @Override
    public AccessTokenResponse getAccessToken() {
        String encodedbase64 = encoder.convertToBase64(String.format("%s:%s", config.getConsumerKey(), config.getConsumerSecret()));
        Request request = new Request.Builder()
                .url(String.format("%s?grant_type=%s", config.getAuthEndpoint(), config.getGrantType()))
                .get()
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BASIC_AUTH_STRING, encodedbase64))
                .addHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_HEADER_VALUE)
                .build();

        try {
            System.out.println("Request URL: " + request.url());
            Response response = client.newCall(request).execute();;
//

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("Response Body: " + responseBody);
                return mapper.readValue(responseBody, AccessTokenResponse.class);

            }
            return null;

        } catch (JsonMappingException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }


    }
}
