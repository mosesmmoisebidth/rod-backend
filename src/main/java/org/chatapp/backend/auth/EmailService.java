package org.chatapp.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from.name}")
    private String fromName;

    @Value("${app.mail.from.address}")
    private String fromAddress;

    @Value("${app.mail.provider:smtp}")
    private String mailProvider;

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

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
        if (!mailEnabled) {
            System.out.println("[EmailService] Email sending is DISABLED via app.mail.enabled=false (env EMAIL_ENABLED). Skipping send to " + to);
            return;
        }
        System.out.println("[EmailService] Provider=" + mailProvider + ", From=" + (fromName != null ? fromName : "") + " <" + fromAddress + ">, To=" + to);
        // Switch by provider
        if ("resend".equalsIgnoreCase(mailProvider)) {
            sendWithResend(to, subject, html);
            return;
        }
        // Default: SMTP via JavaMailSender
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            // For SMTP, fromName may contain just a name or an address
            helper.setFrom(fromAddress);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send email (SMTP): " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[EmailService] Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        String text = html.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">");
        return text.trim().replaceAll("\\s+", " ");
    }

    private void sendWithResend(String to, String subject, String html) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            System.err.println("[EmailService] RESEND provider selected but app.mail.resend.api-key is missing. Skipping send.");
            return;
        }
        try {
            String fromCombined = fromName != null && fromName.contains("<")
                    ? fromName
                    : (fromName + " <" + fromAddress + ">");

            String text = stripHtml(html);

            // Minimal JSON build (escape quotes and backslashes in HTML & TEXT)
            String safeHtml = html
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            String safeText = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");

            // Build transactional headers to improve deliverability
            String headersJson = "{" +
                    "\"X-App\":\"MoChat\"," +
                    "\"Auto-Submitted\":\"auto-generated\"" +
                    (supportEmail != null && !supportEmail.isBlank() ? ",\"List-Unsubscribe\":\"mailto:" + supportEmail + "\"" : "") +
                    "}";

            // Derive a simple tag from subject to help provider classify the message
            String tagValue = subject.toLowerCase().contains("reset") ? "password_reset" : (subject.toLowerCase().contains("verify") ? "verification" : "transactional");
            String tagsJson = "[{\"name\":\"category\",\"value\":\"" + tagValue + "\"}]";

            String json = "{" +
                    "\"from\":\"" + fromCombined + "\"," +
                    "\"to\":[\"" + to + "\"]," +
                    "\"subject\":\"" + subject + "\"," +
                    "\"html\":\"" + safeHtml + "\"," +
                    "\"text\":\"" + safeText + "\"," +
                    (supportEmail != null && !supportEmail.isBlank() ? "\"reply_to\":\"" + supportEmail + "\"," : "") +
                    "\"headers\":" + headersJson + "," +
                    "\"tags\":" + tagsJson +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("[EmailService] Resend email queued successfully: " + response.body());
            } else {
                System.err.println("[EmailService] Resend API error: HTTP " + response.statusCode() + " -> " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send email (Resend): " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[EmailService] Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
        }
    }

    @Async
    public void sendHtmlAsync(String to, String subject, String html) {
        sendHtml(to, subject, html);
    }

    public String buildVerificationEmail(String username, String code) {
        // Minimal transactional template to improve deliverability (no external images or social links)
        return "<html><body style='margin:0;background:" + colorBg + ";color:" + colorText + ";font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif'>" +
                "<div style='max-width:600px;margin:0 auto;padding:24px'>" +
                "<h2 style='margin:0 0 12px 0;color:" + colorPrimary + ";font-size:20px'>Your MoChat verification code</h2>" +
                "<p style='margin:0 0 12px 0'>Hello " + username + ",</p>" +
                "<p style='margin:0 0 12px 0'>Use the code below to verify your email address. This code expires in 10 minutes.</p>" +
                "<div style='margin:16px 0;padding:12px 16px;border:1px solid " + colorAccent + ";border-radius:8px;font-size:26px;font-weight:700;letter-spacing:6px;color:" + colorAccent + ";text-align:center'>" + code + "</div>" +
                "<p style='margin:0 0 12px 0;font-size:12px;opacity:.8'>If you didn’t request this, you can ignore this email.</p>" +
                "<p style='margin:0 0 12px 0;font-size:12px;opacity:.8'>You received this email because a sign-up request was made at MoChat with this address.</p>" +
                "<hr style='border:none;border-top:1px solid #e5e7eb;margin:16px 0'/>" +
                "<p style='margin:0;font-size:12px;opacity:.8'>MoChat • " + address + "</p>" +
                (supportEmail != null && !supportEmail.isBlank() ? "<p style='margin:4px 0 0 0;font-size:12px;opacity:.8'>Questions? Email <a style='color:" + colorPrimary + ";text-decoration:none' href='mailto:" + supportEmail + "'>" + supportEmail + "</a></p>" : "") +
                "</div></body></html>";
    }

    public String buildResetCodeEmail(String username, String code) {
        // Minimal transactional template (no images or social links)
        return "<html><body style='margin:0;background:" + colorBg + ";color:" + colorText + ";font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif'>" +
                "<div style='max-width:600px;margin:0 auto;padding:24px'>" +
                "<h2 style='margin:0 0 12px 0;color:" + colorPrimary + ";font-size:20px'>Your MoChat password reset code</h2>" +
                "<p style='margin:0 0 12px 0'>Hello " + username + ",</p>" +
                "<p style='margin:0 0 12px 0'>Use the code below to reset your password. This code expires in 10 minutes.</p>" +
                "<div style='margin:16px 0;padding:12px 16px;border:1px solid " + colorAccent + ";border-radius:8px;font-size:26px;font-weight:700;letter-spacing:6px;color:" + colorAccent + ";text-align:center'>" + code + "</div>" +
                "<p style='margin:0 0 12px 0;font-size:12px;opacity:.8'>If you didn’t request this, you can ignore this email.</p>" +
                "<p style='margin:0 0 12px 0;font-size:12px;opacity:.8'>You received this email because a password reset was requested for a MoChat account with this address.</p>" +
                "<hr style='border:none;border-top:1px solid #e5e7eb;margin:16px 0'/>" +
                "<p style='margin:0;font-size:12px;opacity:.8'>MoChat • " + address + "</p>" +
                (supportEmail != null && !supportEmail.isBlank() ? "<p style='margin:4px 0 0 0;font-size:12px;opacity:.8'>Questions? Email <a style='color:" + colorPrimary + ";text-decoration:none' href='mailto:" + supportEmail + "'>" + supportEmail + "</a></p>" : "") +
                "</div></body></html>";
    }
}
