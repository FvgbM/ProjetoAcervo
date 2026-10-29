package com.backend.acervo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(unique = true, length = 30)
    private String rg;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false, length = 150)
    private String nome;

    private boolean primeiroLogin = true;
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Perfil perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusUsuario statusUsuario = StatusUsuario.PENDENTE;

    @Column(name = "tentativas_falhas", nullable = false)
    private int tentativasFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    public enum Perfil {
        ALUNO, PROFESSOR, COLABORADOR, ADMIN
    }

    public enum StatusUsuario {
        ATIVO, INATIVO, PENDENTE, BLOQUEADO
    }
}