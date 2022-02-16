package com.chty.autocard.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Component
public class EmailClient {
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${app.mail.from}")
    private String from;
    
    @Value("${app.mail.nickname}")
    private String nickname;
    
    public void send(String to, String subject, String text) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setFrom(nickname + "<" + from + ">");
        mimeMessageHelper.setSubject(subject);
        
        mimeMessage = mimeMessageHelper.getMimeMessage();

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(text, "text/html;charset=UTF-8");

        MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.addBodyPart(mimeBodyPart);
        
        mimeMessage.setContent(mimeMultipart);
        mimeMessage.saveChanges();
        
        javaMailSender.send(mimeMessage);
    }
    
}
