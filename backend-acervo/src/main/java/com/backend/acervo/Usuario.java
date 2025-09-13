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
    
    @Column(nullable = false)
    private String senha;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false, unique = true)
    private String rg; // RG.SP
    
    @Column(nullable = false)
    private boolean primeiroLogin = true;
    
    @Column(nullable = false)
    private boolean ativo = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;
    
    public enum Papel {
        ALUNO, PROFESSOR, COLABORADOR
    }

    private String tokenRecuperacao;

    private LocalDateTime dataExpiracaoToken;

}