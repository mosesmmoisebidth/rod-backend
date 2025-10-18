package org.chatapp.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from.name}")
    private String fromName;

    @Value("${app.brand.logo-url}")
    private String logoUrl;
    @Value("${app.brand.site-url}")
    private String siteUrl;
    @Value("${app.brand.support-email}")
    private String supportEmail;
    @Value("${app.brand.address}")
    private String address;
    @Value("${app.brand.instagram}")
    private String instagram;
    @Value("${app.brand.linkedin}")
    private String linkedin;
    @Value("${app.brand.color.bg}")
    private String colorBg;
    @Value("${app.brand.color.text}")
    private String colorText;
    @Value("${app.brand.color.primary}")
    private String colorPrimary;
    @Value("${app.brand.color.accent}")
    private String colorAccent;

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromName);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public String buildVerificationEmail(String username, String code) {
        return "<html><body style='margin:0;background:" + colorBg + ";color:" + colorText + ";font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' role='presentation'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' role='presentation' style='max-width:600px;width:100%'>" +
                "<tr><td style='padding:24px 24px 0;text-align:center'>" +
                "<img alt='MoChat' src='" + logoUrl + "' style='height:48px'/></td></tr>" +
                "<tr><td style='padding:24px;background:#0b0f0c;border-radius:12px;border:1px solid #111'>" +
                "<h2 style='margin:0 0 8px 0;color:" + colorPrimary + ";font-size:22px'>Verify your MoChat email</h2>" +
                "<p style='margin:0 0 16px 0;opacity:.9'>Hello " + username + ", thanks for registering to MoChat. Use the code below to verify your email. It expires in 10 minutes.</p>" +
                "<div style='margin:12px 0;padding:12px 16px;border:1px dashed " + colorAccent + ";border-radius:8px;font-size:28px;font-weight:700;letter-spacing:6px;color:" + colorAccent + ";text-align:center'>" + code + "</div>" +
                "<p style='margin:8px 0 0 0;font-size:12px;opacity:.7'>If you didn’t create an account, you can safely ignore this email.</p>" +
                "</td></tr>" +
                "<tr><td style='padding:16px;text-align:center;font-size:12px;opacity:.7'>" +
                "<a href='" + siteUrl + "' style='color:" + colorPrimary + ";text-decoration:none'>MoChat</a> • " + address +
                "<br/>Support: <a style='color:" + colorPrimary + ";text-decoration:none' href='mailto:" + supportEmail + "'>" + supportEmail + "</a>" +
                "<br/><a style='color:" + colorPrimary + ";text-decoration:none' href='" + instagram + "'>Instagram</a> • <a style='color:" + colorPrimary + ";text-decoration:none' href='" + linkedin + "'>LinkedIn</a>" +
                "</td></tr></table></td></tr></table></body></html>";
    }

    public String buildResetCodeEmail(String username, String code) {
        return "<html><body style='margin:0;background:" + colorBg + ";color:" + colorText + ";font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' role='presentation'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' role='presentation' style='max-width:600px;width:100%'>" +
                "<tr><td style='padding:24px 24px 0;text-align:center'>" +
                "<img alt='MoChat' src='" + logoUrl + "' style='height:48px'/></td></tr>" +
                "<tr><td style='padding:24px;background:#0b0f0c;border-radius:12px;border:1px solid #111'>" +
                "<h2 style='margin:0 0 8px 0;color:" + colorPrimary + ";font-size:22px'>Reset your MoChat password</h2>" +
                "<p style='margin:0 0 16px 0;opacity:.9'>Hello " + username + ", use the code below to reset your password. It expires in 10 minutes.</p>" +
                "<div style='margin:12px 0;padding:12px 16px;border:1px dashed " + colorAccent + ";border-radius:8px;font-size:28px;font-weight:700;letter-spacing:6px;color:" + colorAccent + ";text-align:center'>" + code + "</div>" +
                "<p style='margin:8px 0 0 0;font-size:12px;opacity:.7'>If you didn’t request this, you can ignore this email.</p>" +
                "</td></tr>" +
                "<tr><td style='padding:16px;text-align:center;font-size:12px;opacity:.7'>" +
                "<a href='" + siteUrl + "' style='color:" + colorPrimary + ";text-decoration:none'>MoChat</a> • " + address +
                "<br/>Support: <a style='color:" + colorPrimary + ";text-decoration:none' href='mailto:" + supportEmail + "'>" + supportEmail + "</a>" +
                "<br/><a style='color:" + colorPrimary + ";text-decoration:none' href='" + instagram + "'>Instagram</a> • <a style='color:" + colorPrimary + ";text-decoration:none' href='" + linkedin + "'>LinkedIn</a>" +
                "</td></tr></table></td></tr></table></body></html>";
    }
}
