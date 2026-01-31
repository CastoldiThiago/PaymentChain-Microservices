package com.paymentchain.notification.service;

import com.paymentchain.notification.dtos.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Default "from" address, and a fallback recipient if none provided
    @Value("${spring.mail.username:noreply@paymentchain.com}")
    private String from;

    @Value("${mail.default-recipient:recruiter@example.com}")
    private String defaultRecipient;

    public void sendTransactionNotification(TransactionResponse tx, String to) {
        String recipient = (to != null && !to.isEmpty()) ? to : defaultRecipient;
        if (recipient == null || recipient.isEmpty()) return; // nothing to do

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipient);
        msg.setFrom(from);
        msg.setSubject("[PaymentChain] Transaction processed: " + safe(tx.getReference()));
        msg.setText(buildBody(tx));

        mailSender.send(msg);
    }

    private String buildBody(TransactionResponse tx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reference: ").append(safe(tx.getReference())).append('\n');
        sb.append("Account IBAN: ").append(safe(tx.getAccountIban())).append('\n');
        sb.append("Amount: ").append(safe(tx.getAmount())).append('\n');
        sb.append("Fee: ").append(safe(tx.getFee())).append('\n');
        sb.append("Total: ").append(safe(tx.getTotal())).append('\n');
        sb.append("Status: ").append(safe(tx.getStatus())).append('\n');
        sb.append("Currency: ").append(safe(tx.getCurrency())).append('\n');
        return sb.toString();
    }

    private Object safe(Object o) {
        return o == null ? "-" : o;
    }
}
