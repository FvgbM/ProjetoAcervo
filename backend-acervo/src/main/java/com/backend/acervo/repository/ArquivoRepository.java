package com.backend.acervo.repository;

import com.backend.acervo.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {

}