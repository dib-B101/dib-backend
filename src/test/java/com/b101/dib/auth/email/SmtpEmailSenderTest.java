package com.b101.dib.auth.email;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailSenderTest {

    @Test
    void sendsKoreanResetMessageWithPlainTextAndHtml() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        given(mailSender.createMimeMessage()).willReturn(message);

        String link = "https://app.example.com/password/reset?token=" + "a".repeat(43);
        new SmtpEmailSender(mailSender, "sender@example.com", "test-app-password")
                .sendPasswordResetLink("member@example.com", link);
        message.saveChanges();

        verify(mailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("[DIB] 비밀번호 재설정 안내");
        assertThat(message.getFrom()[0].toString()).contains("sender@example.com");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("member@example.com");
        assertThat(partText(message, "text/plain")).contains(link, "30분", "요청하지 않으셨다면");
        assertThat(partText(message, "text/html")).contains(link, "비밀번호 재설정하기");
    }

    private String partText(Part part, String type) throws Exception {
        Object content = part.getContent();
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                String result = partText(multipart.getBodyPart(i), type);
                if (result != null) return result;
            }
        }
        if (part.isMimeType(type)) return content.toString();
        return null;
    }
}
