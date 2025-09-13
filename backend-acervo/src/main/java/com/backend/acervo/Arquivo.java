package com.backend.acervo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "arquivos")
@Data
public class Arquivo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomeArmazenado;
    private String nomeOriginal;
    private String tipoConteudo;
    private Long tamanho;
    private String hash;
    private boolean criptografado = true;

    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario enviadoPor;

    private LocalDateTime dataEnvio;
}