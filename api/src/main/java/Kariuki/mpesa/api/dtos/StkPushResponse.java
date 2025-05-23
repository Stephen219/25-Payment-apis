package Kariuki.mpesa.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data


public class StkPushResponse {
    @JsonProperty("MerchantRequestID")
    private String MerchantRequestID;
    @JsonProperty("CheckoutRequestID")
    private String CheckoutRequestID;
    @JsonProperty("ResponseCode")
    private String ResponseCode;
    @JsonProperty("ResponseDescription")
    private String ResponseDescription;
    @JsonProperty("CustomerMessage")
    private String CustomerMessage;
}
