package Kariuki.mpesa.api.accessToken;

import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.MpesaValidationResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j


public class TokenController {

    private final TokenService service;
    private final AcknowledgeResponse acknowledgeResponse;


@Autowired
    public TokenController(TokenService service, AcknowledgeResponse acknowledgeResponse) {
        this.service = service;
    this.acknowledgeResponse = acknowledgeResponse;
}


    @GetMapping("/access_token")
    public ResponseEntity<AccessTokenResponse> getApiToken(){
        return ResponseEntity.ok(service.getAccessToken());

    }
    @GetMapping("/register_url")
    public ResponseEntity<RegisterUrlResponse> getregisterUrl(){
    return ResponseEntity.ok(service.registerUrl());
    }

//    @PostMapping("/validation")
//    public ResponseEntity<AcknowledgeResponse> validation(@RequestBody MpesaValidationResponse validationResponse)
//
//
//    {
//
//        System.out.println(validationResponse + "valiation responde is here   ");
//        return ResponseEntity.ok(acknowledgeResponse);
//    }

    @PostMapping("/validation")
    public ResponseEntity<AcknowledgeResponse> validation(HttpServletRequest request) throws IOException {
        System.out.println(new String(request.getInputStream().readAllBytes()) + " this is my raw request body");
        return ResponseEntity.ok(acknowledgeResponse);
    }



    @PostMapping(path = "/simulate-c2b", produces = "application/json")
    public ResponseEntity<C2bResponse> simulateB2CTransaction(@RequestBody C2Brequest simulateTransactionRequest) {
        System.out.println(simulateTransactionRequest);
        return ResponseEntity.ok(service.c2bsimulation(simulateTransactionRequest));
    }






}
