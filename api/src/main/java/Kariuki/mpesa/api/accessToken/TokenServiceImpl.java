package Kariuki.mpesa.api.accessToken;

import Kariuki.mpesa.api.config.MpesaConfig;
import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlRequest;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;
import Kariuki.mpesa.api.utils.EncoderUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

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
    public static final String BEARER_AUTH_STRING = "Bearer";
    public static MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

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

    @Override
    public RegisterUrlResponse registerUrl() {
        AccessTokenResponse token = getAccessToken();

        RegisterUrlRequest request = new RegisterUrlRequest();
        request.setConfirmationURL(config.getConfirmationUrl());
        request.setValidationURL(config.getValidationUrl());
        request.setResponseType(config.getResponseType());
        request.setShortCode(config.getShortCode());
        System.out.println("tetst request we see     "+ request.toString());
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, Objects.requireNonNull(encoder.toJson(request)));

        Request requestToSend = new Request.Builder()
                .url(config.getRegisterUrl())
                .post(requestBody)
                .addHeader("Authorization", String.format("Bearer %s", token.getAccessToken()))
                .build();


        try {
            System.out.println("the request being sent to thr " + requestToSend);
            Response response = client.newCall(requestToSend).execute();
            String responsebody = response.body().string();
            System.out.println("this is i "  + responsebody + "    hapa ndo response iki");
           return mapper.readValue(responsebody, RegisterUrlResponse.class);





        } catch (IOException e) {
            System.out.println(String.format("Could not register url -> %s", e.getLocalizedMessage()));
            throw new RuntimeException(e);

        }


    }

    /**
     * @param
     * @return
     */
    @Override
    public C2bResponse c2bsimulation(C2Brequest c2brequest) {
       AccessTokenResponse token = getAccessToken();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE,
                Objects.requireNonNull(encoder.toJson(c2brequest)));
        Request request = new Request.Builder()
                .url(config.getSimulateTransactionEndpoint())
                .post(body)
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BEARER_AUTH_STRING, token.getAccessToken()))
                .build();


        try {
            System.out.println(" this is the request sent" + request);
            printRequestBody(request.body());
//            System.out.println(new okio.Buffer().writeTo(request.body().source()).readUtf8() + " this is my request body");



            System.out.println(request);
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            System.out.println(responseBody+ "      thiu is the rsponse body");
            assert response.body() != null;
            // use Jackson to Decode the ResponseBody ...

            return mapper.readValue(responseBody, C2bResponse.class);
        } catch (IOException e) {
            log.error(String.format("Could not simulate C2B transaction -> %s", e.getLocalizedMessage()));
            return null;
        }

    }






    public void printRequestBody(RequestBody request) throws IOException {
        Buffer buffer = new Buffer();
        // Write the request body content to the buffer
        request.writeTo(buffer);
        // Print the content of the request body
        System.out.println(buffer.readUtf8() + " this is my request body");
    }

}
