package com.electricalwarehouse.integration;

import com.electricalwarehouse.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Sends approval e-mails asynchronously. Failures are logged, never propagated to the workflow. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties props;

    @Async
    public void send(String to, String subject, String body) {
        if (!props.mail().enabled() || !StringUtils.hasText(to)) {
            log.debug("Mail disabled or no recipient, skipped: {}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(props.mail().from());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Cannot send mail to {}: {}", to, e.getMessage());
        }
    }
}
