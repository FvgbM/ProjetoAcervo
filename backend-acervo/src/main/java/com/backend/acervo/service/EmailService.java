package com.backend.acervo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarEmailRecuperacao(String destinatario, String linkRecuperacao) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Redefinição de senha — Sistema Acervo");
        mensagem.setText(
                "Recebemos uma solicitação de redefinição de senha.\n\n"
                        + "Acesse o link abaixo para criar uma nova senha:\n"
                        + linkRecuperacao + "\n\n"
                        + "O link expira em duas horas e só pode ser usado uma vez. "
                        + "Se você não solicitou a alteração, ignore esta mensagem."
        );
        mailSender.send(mensagem);
    }
}