package com.b101.dib.auth.sms;

public interface SmsSender {

    void send(String phoneNumber, String message);
}
