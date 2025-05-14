package Kariuki.mpesa.api.accessToken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j


public class TokenController {

    private final TokenService service;


@Autowired
    public TokenController(TokenService service) {
        this.service = service;
    }


    @GetMapping("access_token")
    public ResponseEntity<AccessTokenResponse> getApiToken(){
        return ResponseEntity.ok(service.getAccessToken());

    }

}
