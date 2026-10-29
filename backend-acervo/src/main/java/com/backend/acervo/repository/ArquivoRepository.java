package com.backend.acervo.repository;

import com.backend.acervo.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
    
    Optional<Arquivo> findByNomeArmazenado(String nomeArmazenado);
    Optional<Arquivo> findByAtaId(Long ataId);
    boolean existsByNomeArmazenado(String nomeArmazenado);
    boolean existsByAtaId(Long ataId);
}