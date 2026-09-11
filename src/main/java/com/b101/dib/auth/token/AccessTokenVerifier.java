package com.b101.dib.auth.token;

public interface AccessTokenVerifier {

    AccessTokenClaims verifyBearer(String authorizationHeader);
}

