package com.b101.dib.auth.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "auth.mail.mode", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress,
            @Value("${spring.mail.password:}") String appPassword
    ) {
        if (fromAddress.isBlank() || appPassword.isBlank()) {
            throw new IllegalStateException("SMTP_USERNAME과 SMTP_APP_PASSWORD를 설정해야 합니다.");
        }
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendPasswordResetLink(String email, String resetLink) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "DIB");
            helper.setTo(email);
            helper.setSubject("[DIB] 비밀번호 재설정 안내");
            helper.setText(plainText(resetLink), htmlText(resetLink));
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("비밀번호 재설정 메일을 작성하지 못했습니다.", e);
        }
        mailSender.send(message);
    }

    private String plainText(String resetLink) {
        return "DIB 비밀번호 재설정\n\n"
                + "비밀번호 재설정을 요청하셨습니다. 아래 링크를 DIB Android 앱에서 열어 새 비밀번호를 설정해주세요.\n\n"
                + resetLink + "\n\n"
                + "이 링크는 30분 동안 유효하며 한 번만 사용할 수 있습니다. "
                + "요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.\n\n"
                + "DIB 팀";
    }

    private String htmlText(String resetLink) {
        String safeLink = resetLink.replace("&", "&amp;").replace("\"", "&quot;")
                .replace("<", "&lt;").replace(">", "&gt;");
        return "<!doctype html><html lang=\"ko\"><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family:Arial,sans-serif;color:#17212b;line-height:1.6\">"
                + "<h2>DIB 비밀번호 재설정</h2>"
                + "<p>비밀번호 재설정을 요청하셨습니다. 아래 버튼을 눌러 DIB Android 앱에서 새 비밀번호를 설정해주세요.</p>"
                + "<p><a href=\"" + safeLink + "\" style=\"display:inline-block;padding:12px 20px;"
                + "background:#172f50;color:#ffffff;text-decoration:none;border-radius:8px\">비밀번호 재설정하기</a></p>"
                + "<p>링크는 30분 동안 유효하며 한 번만 사용할 수 있습니다.</p>"
                + "<p>요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.</p>"
                + "<p style=\"color:#667788\">DIB 팀</p></body></html>";
    }
}
