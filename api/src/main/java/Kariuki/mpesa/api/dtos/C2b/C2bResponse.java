package Kariuki.mpesa.api.dtos.C2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data

public class C2bResponse {
    @JsonProperty("ResponseCode")
    private String responsecode;

    @JsonProperty("ResponseDescription")
    private String responseDescription;

    @JsonProperty("OriginatorCoversationID")
    private String originatorCoversationID;
}
