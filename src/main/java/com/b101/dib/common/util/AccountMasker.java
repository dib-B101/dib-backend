package com.b101.dib.common.util;

public final class AccountMasker {
    private AccountMasker() {}

    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 5) {
            return accountNumber;
        }
        int keep = 4;
        StringBuilder sb = new StringBuilder();
        int end = accountNumber.length() - keep;
        for (int i = 0; i < accountNumber.length(); i++) {
            char c = accountNumber.charAt(i);
            sb.append(i < end && Character.isDigit(c) ? '*' : c);
        }
        return sb.toString();
    }
}
