package dev.d76.authx.platform.notification.email;

public interface MailSender {
    void send(MailMessage message);
}