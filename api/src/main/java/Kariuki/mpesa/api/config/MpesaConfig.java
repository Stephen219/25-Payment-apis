package Kariuki.mpesa.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa")
public class MpesaConfig {
    String consumerKey;
    String consumerSecret;
    String grantType;
    String authEndpoint;
    String shortCode;
    String responseType;
    String ValidationUrl;
    String confirmationUrl;
    String registerUrl;
    String simulateTransactionEndpoint;
    private String stkPushEndpoint;


    public String toString() {
        return "MpesaConfig{" +
                "consumerKey='" + consumerKey + '\'' +
                ", consumerSecret='" + consumerSecret + '\'' +
                ", grantType='" + grantType + '\'' +
                ", authEndpoint='" + authEndpoint + '\'' +
                '}';
    }
}
