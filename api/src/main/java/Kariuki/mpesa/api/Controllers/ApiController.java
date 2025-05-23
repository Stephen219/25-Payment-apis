package Kariuki.mpesa.api.Controllers;

import Kariuki.mpesa.api.Services.TokenService;
import Kariuki.mpesa.api.Services.TokenServiceImpl;
import Kariuki.mpesa.api.dtos.StkPushRequest;
import Kariuki.mpesa.api.dtos.StkPushResponse;
import Kariuki.mpesa.api.dtos.Token.AccessTokenResponse;
import Kariuki.mpesa.api.dtos.Token.AcknowledgeResponse;
import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j


public class ApiController {

    private final TokenService service;
    private final AcknowledgeResponse acknowledgeResponse;


@Autowired
    public ApiController(TokenService service, AcknowledgeResponse acknowledgeResponse) {
        this.service = service;
    this.acknowledgeResponse = acknowledgeResponse;
}



    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> not(){
        return new ResponseEntity<>("OK, 200", HttpStatus.OK);

    }

    @GetMapping("/access_token")
    public ResponseEntity<AccessTokenResponse> getApiToken(){
        return ResponseEntity.ok(service.getAccessToken());

    }
    @GetMapping("/register_url")
    public ResponseEntity<RegisterUrlResponse> getregisterUrl(){
    return ResponseEntity.ok(service.registerUrl());
    }

    @PostMapping("/validation")
    public Map<String, Object> validation(@RequestBody Map<String, Object> payload) {
        System.out.println("Validation Payload: " + payload);
        return Map.of("ResultCode", 0, "ResultDesc", "Accepted");
    }
 // tecnically i will use a dto   but for now i need validation to wor
//    TODO:  make this a dtop
    @PostMapping("/confirmation")
    public Map<String, Object> confirmation(@RequestBody Map<String, Object> payload) {
        System.out.println("Confirmation Payload: " + payload);
        return Map.of("ResultCode", 0, "ResultDesc", "Success");
    }
    @PostMapping(value = "/stk", produces = "application/json")
    public ResponseEntity<StkPushResponse> stkPush(@RequestBody StkPushRequest stkPushRequest) {
        return ResponseEntity.ok(service.stkPush(stkPushRequest));
    }



    @PostMapping(path = "/simulate-c2b", produces = "application/json")
    public ResponseEntity<C2bResponse> simulateB2CTransaction(@RequestBody C2Brequest simulateTransactionRequest) {
        System.out.println(simulateTransactionRequest);
        return ResponseEntity.ok(service.simulateC2BTransaction(simulateTransactionRequest));
    }






    @PostMapping("/test-simulate")
    public C2bResponse testSimulate() {
        C2Brequest request = new C2Brequest();
        request.setShortCode("107031");
        request.setMsisdn("254708374149");
        request.setAmount("100");
        request.setBillRefNumber("TEST123");
        request.setCommandID("CustomerPayBillOnline");
        return service.c2bsimulation(request);
    }




}
