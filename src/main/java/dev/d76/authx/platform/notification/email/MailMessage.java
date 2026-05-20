package dev.d76.authx.platform.notification.email;

import dev.d76.authx.platform.notification.email.MailContentType;

public record MailMessage(
        String to,
        String subject,
        String body,
        MailContentType contentType
) {
}