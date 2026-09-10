package com.b101.dib.auth.domain;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\p{Punct}).{10,64}$"
    );

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
