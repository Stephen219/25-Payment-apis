//

package Kariuki.mpesa.api.Services;

import Kariuki.mpesa.api.dtos.StkPushRequest;
import Kariuki.mpesa.api.dtos.StkPushResponse;
import Kariuki.mpesa.api.dtos.Token.AccessTokenResponse;
import Kariuki.mpesa.api.config.MpesaConfig;
import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlRequest;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;
import Kariuki.mpesa.api.utils.EncoderUtil;
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
    private final EncoderUtil encoder;
    private AccessTokenResponse cachedToken;
    private boolean urlsRegistered;

    public static final String BASIC_AUTH_STRING = "Basic";
    public static final String AUTHORIZATION_HEADER_STRING = "authorization";
    public static final String CACHE_CONTROL_HEADER = "cache-control";
    public static final String CACHE_CONTROL_HEADER_VALUE = "no-cache";
    public static final String BEARER_AUTH_STRING = "Bearer";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public TokenServiceImpl(OkHttpClient client, MpesaConfig config, ObjectMapper mapper) {
        this.client = client;
        this.config = config;
        this.mapper = mapper;
        this.encoder = new EncoderUtil();
        this.urlsRegistered = false;
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
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("Access Token Response: {}", responseBody);
                return mapper.readValue(responseBody, AccessTokenResponse.class);
            }
            log.error("Failed to get access token: {}", response.body().string());
            return null;
        } catch (IOException ex) {
            log.error("Error getting access token: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public RegisterUrlResponse registerUrl() {
        AccessTokenResponse token = getAccessToken();
        if (token == null) {
            throw new RuntimeException("Failed to get access token for URL registration");
        }

        RegisterUrlRequest request = new RegisterUrlRequest();
        request.setConfirmationURL(config.getConfirmationUrl());
        request.setValidationURL(config.getValidationUrl());
        request.setResponseType(config.getResponseType());
        request.setShortCode(config.getShortCode());
        log.info("Register URL Request: {}", request);

        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, Objects.requireNonNull(encoder.toJson(request)));
        Request requestToSend = new Request.Builder()
                .url(config.getRegisterUrl())
                .post(requestBody)
                .addHeader("Authorization", String.format("Bearer %s", token.getAccessToken()))
                .build();

        try {
            Response response = client.newCall(requestToSend).execute();
            String responseBody = response.body().string();
            log.info("Register URL Response: {}", responseBody);
            RegisterUrlResponse registerResponse = mapper.readValue(responseBody, RegisterUrlResponse.class);
            if ("Success".equals(registerResponse.getResponseDescription())) {
                urlsRegistered = true;
            }
            return registerResponse;
        } catch (IOException ex) {
            log.error("Could not register URL: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param c2brequest
     * @return
     */
    @Override
    public C2bResponse simulateC2BTransaction(C2Brequest c2brequest) {
        return null;
    }

    @Override
    public C2bResponse c2bsimulation(C2Brequest c2brequest) {
        // Ensure URLs are registered
        if (!urlsRegistered) {
            RegisterUrlResponse registerResponse = registerUrl();
            log.error("jfnjrfjjfnfr this is our response at method level" + registerResponse);
            if (registerResponse == null) {
                throw new RuntimeException("Failed to register URLs");
            }
        }


        if (cachedToken == null) {
            cachedToken = getAccessToken();
            if (cachedToken == null) {
                throw new RuntimeException("Failed to get access token");
            }
        }

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, Objects.requireNonNull(encoder.toJson(c2brequest)));
        Request request = new Request.Builder()
                .url(config.getSimulateTransactionEndpoint())
                .post(body)
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BEARER_AUTH_STRING, cachedToken.getAccessToken()))
                .build();

        try {
            log.info("C2B Simulation Request: {}", c2brequest);
            printRequestBody(body);
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("C2B Simulation Response: {}", responseBody);
            return mapper.readValue(responseBody, C2bResponse.class);
        } catch (IOException ex) {
            log.error("Could not simulate C2B transaction: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void printRequestBody(RequestBody request) throws IOException {
        Buffer buffer = new Buffer();
        request.writeTo(buffer);
        log.info("Request Body: {}", buffer.readUtf8());
    }







    @Override
    public StkPushResponse stkPush(StkPushRequest stkPushRequest) {
        AccessTokenResponse accessToken = getAccessToken();
        if (accessToken == null) {
            log.error("Failed to retrieve access token");
        }
        RequestBody requestBody = RequestBody.create(Objects.requireNonNull(encoder.toJson(stkPushRequest)),
                MediaType.get("application/json; charset=utf-8"));

        assert accessToken != null;
        Request request = new Request.Builder()
                .url(config.getStkPushEndpoint())
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessToken.getAccessToken())
                .addHeader("Content-Type", "application/json")
                .build();
        log.info("Sending STK push request to: {}", config.getStkPushEndpoint());
        log.info("Request body: {}", requestBody);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details provided";
                log.error("Request failed with status code: {}", response.code());
                log.error("Response body: {}", errorBody);
                throw new RuntimeException("Error occurred while making STK push request");
            }
            String responseBody = response.body().string();
            return mapper.readValue(responseBody, StkPushResponse.class);
        } catch (IOException e) {
            log.error("Error occurred while making STK push request: {}", e.getMessage());
            return null;
        }
    }


}