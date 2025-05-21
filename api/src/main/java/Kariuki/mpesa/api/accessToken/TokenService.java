package Kariuki.mpesa.api.accessToken;

import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlRequest;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;

public interface TokenService {
    AccessTokenResponse getAccessToken();

    RegisterUrlResponse registerUrl();

    C2bResponse c2bsimulation(C2Brequest request);
}
