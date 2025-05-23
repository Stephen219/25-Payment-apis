package Kariuki.mpesa.api.Services;

import Kariuki.mpesa.api.dtos.C2b.C2Brequest;
import Kariuki.mpesa.api.dtos.C2b.C2bResponse;
import Kariuki.mpesa.api.dtos.RegisterUrlResponse;
import Kariuki.mpesa.api.dtos.StkPushRequest;
import Kariuki.mpesa.api.dtos.StkPushResponse;
import Kariuki.mpesa.api.dtos.Token.AccessTokenResponse;

public interface TokenService {


    AccessTokenResponse getAccessToken();

    RegisterUrlResponse registerUrl();

    C2bResponse c2bsimulation(C2Brequest request);

    C2bResponse simulateC2BTransaction(C2Brequest c2brequest);
    StkPushResponse stkPush(StkPushRequest stkPushRequest);
}

