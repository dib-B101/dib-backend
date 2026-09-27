package com.b101.dib.auth.command.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAdminPhoneTokenVerifier implements FirebasePhoneTokenVerifier {

    private final String projectId;
    private volatile FirebaseAuth firebaseAuth;

    public FirebaseAdminPhoneTokenVerifier(@Value("${firebase.project-id:}") String projectId) {
        this.projectId = projectId;
    }

    @Override
    public VerifiedPhone verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
        }
        try {
            FirebaseToken token = auth().verifyIdToken(idToken);
            Object firebaseClaim = token.getClaims().get("firebase");
            Object provider = firebaseClaim instanceof Map<?, ?> claims ? claims.get("sign_in_provider") : null;
            Object phone = token.getClaims().get("phone_number");
            Object authTime = token.getClaims().get("auth_time");
            if (!"phone".equals(provider) || !(phone instanceof String number)
                    || !(authTime instanceof Number seconds) || token.getUid() == null) {
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
            }
            return new VerifiedPhone(token.getUid(), number, Instant.ofEpochSecond(seconds.longValue()));
        } catch (FirebaseAuthException e) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
        }
    }

    private FirebaseAuth auth() {
        FirebaseAuth current = firebaseAuth;
        if (current != null) return current;
        synchronized (this) {
            if (firebaseAuth != null) return firebaseAuth;
            if (projectId.isBlank()) throw new BusinessException(ErrorCode.FIREBASE_UNAVAILABLE);
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(projectId)
                        .build();
                FirebaseApp app = FirebaseApp.initializeApp(options, "dib-phone-auth");
                firebaseAuth = FirebaseAuth.getInstance(app);
                return firebaseAuth;
            } catch (IOException | IllegalStateException e) {
                throw new BusinessException(ErrorCode.FIREBASE_UNAVAILABLE);
            }
        }
    }
}
