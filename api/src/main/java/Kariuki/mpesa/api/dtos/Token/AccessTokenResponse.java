package Kariuki.mpesa.api.dtos.Token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data


public class AccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private String expiresIn;


}
