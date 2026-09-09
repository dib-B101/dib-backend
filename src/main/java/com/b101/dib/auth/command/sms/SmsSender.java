package com.b101.dib.auth.command.sms;

public interface SmsSender {

    void send(String phoneNumber, String message);
}
