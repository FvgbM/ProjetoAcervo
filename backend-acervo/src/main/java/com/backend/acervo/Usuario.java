package com.backend.acervo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email; // @fatec.sp.gov.br
    private String rg; // RG.SP
    
    @Column(nullable = false)
    private String senha;
    private String nome;
    private String status; // Ativo, Inativo, Pendente
    private boolean primeiroLogin = true;
    private boolean ativo = true;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    public enum Perfil {
        ALUNO, PROFESSOR, COLABORADOR, ADMIN
    }
    private String tokenRecuperacao;
    private LocalDateTime dataExpiracaoToken;

    public enum StatusUsuario {
       ATIVO, INATIVO, PENDENTE, BLOQUEADO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusUsuario statusUsuario = StatusUsuario.PENDENTE; // Valor padrão
}