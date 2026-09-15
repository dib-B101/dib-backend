package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.PasswordResetLinkRequest;
import com.b101.dib.auth.command.dto.PasswordResetRequest;

public interface PasswordResetService {

    void requestResetLink(PasswordResetLinkRequest request);

    void resetPassword(PasswordResetRequest request);
}
