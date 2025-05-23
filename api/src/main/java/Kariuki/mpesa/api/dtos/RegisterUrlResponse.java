package Kariuki.mpesa.api.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data


public class RegisterUrlResponse {
    @JsonProperty("ResponseCode")
    private String conversationID;

    @JsonProperty("ResponseDescription")
    private String responseDescription;

    @JsonProperty("OriginatorCoversationID")
    private String originatorCoversationID;
}
