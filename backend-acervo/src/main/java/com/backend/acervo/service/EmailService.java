package com.backend.acervo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacao(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Redefinição de Senha - Sistema Acervo");
        message.setText("Para redefinir sua senha, use o token: " + token +
                  "\n\nOu acesse: http://localhost:4200/recuperar-senha?token=" + token +
                  "\n\nEste token expira em 2 horas.");
        
        mailSender.send(message);
    }

    public void enviarEmailAcesso(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Primeiro Acesso - Sistema Acervo");
        message.setText("Seu acesso foi criado com a senha temporária: " + temporaryPassword +
                      "\n\nVocê será solicitado a alterá-la no primeiro login.");
        
        mailSender.send(message);
    }
}