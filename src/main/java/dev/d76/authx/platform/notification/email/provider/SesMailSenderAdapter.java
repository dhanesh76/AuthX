package dev.d76.authx.platform.notification.email.provider;

import dev.d76.authx.platform.notification.email.MailMessage;
import dev.d76.authx.platform.notification.email.MailSender;

//@Component
//@RequiredArgsConstructor
public class SesMailSenderAdapter implements MailSender {
    @Override
    public void send(MailMessage message) {

    }
//
//    private final SesClient sesClient;
//
//    @Async
//    @Override
//    public void send(MailMessage message) {
//        var body = MailContentType.HTML.equals(message.contentType())
//                ? Body.builder()
//                .html(c -> c.data(message.body()))
//                .build()
//                : Body.builder()
//                .text(c -> c.data(message.body()))
//                .build();
//
//        sesClient.sendEmail(r -> r
//                .destination(d -> d.toAddresses(message.to()))
//                .message(m -> m
//                        .subject(c -> c.data(message.subject()))
//                        .body(body)
//                )
//        );
//    }
}