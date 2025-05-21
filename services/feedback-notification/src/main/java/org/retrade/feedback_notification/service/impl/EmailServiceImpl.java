package org.retrade.feedback_notification.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.feedback_notification.model.message.EmailNotificationMessage;
import org.retrade.feedback_notification.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendEmail(EmailNotificationMessage emailNotification) {
        try {
            log.info("Preparing to send email to: {}, subject: {}", 
                    emailNotification.getTo(), emailNotification.getSubject());

            final Context context = new Context();

            if (emailNotification.getTemplateVariables() != null) {
                emailNotification.getTemplateVariables().forEach(context::setVariable);
            }

            final String templateName = emailNotification.getTemplateName();
            final String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                    StandardCharsets.UTF_8.name());
            
            helper.setTo(emailNotification.getTo());
            helper.setSubject(emailNotification.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", emailNotification.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailNotification.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
