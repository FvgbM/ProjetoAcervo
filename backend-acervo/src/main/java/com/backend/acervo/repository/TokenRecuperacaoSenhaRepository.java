package com.backend.acervo.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.acervo.TokenRecuperacaoSenha;

public interface TokenRecuperacaoSenhaRepository
        extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findFirstByTokenHashAndUsadoEmIsNull(String tokenHash);

    @Modifying
    @Query("""
        update TokenRecuperacaoSenha t
           set t.usadoEm = :agora
         where t.usuario.id = :usuarioId
           and t.usadoEm is null
    """)
    void invalidarTokensAtivosDoUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("agora") LocalDateTime agora
    );
}