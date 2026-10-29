package com.backend.acervo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "arquivos")
@Getter
@Setter
public class Arquivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nomeOriginal;

    @Column(nullable = false, unique = true, length = 255)
    private String nomeArmazenado;

    @Column(nullable = false, length = 100)
    private String tipoConteudo;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false)
    private boolean criptografado = true;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ata_id", nullable = false, unique = true)
    private Ata ata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}