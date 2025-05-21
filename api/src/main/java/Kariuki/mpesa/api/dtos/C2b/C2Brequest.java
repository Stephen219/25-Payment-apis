package Kariuki.mpesa.api.dtos.C2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class C2Brequest {
    @JsonProperty("ShortCode")
    private String shortCode;
    @JsonProperty("Msisdn")
    private String msisdn;
    @JsonProperty("BillRefNumber")
    private String billRefNumber;
    @JsonProperty("Amount")
    private String amount;
    @JsonProperty("CommandID")
    private String commandID;
}
