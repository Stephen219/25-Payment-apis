package Kariuki.mpesa.api.dtos.C2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data

public class C2bResponse {
    @JsonProperty("ConversationID")
    private String conversationID;

    @JsonProperty("ResponseDescription")
    private String responseDescription;

    @JsonProperty("OriginatorCoversationID")
    private String originatorCoversationID;
}
