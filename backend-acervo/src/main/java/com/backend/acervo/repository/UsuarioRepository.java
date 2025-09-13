package com.backend.acervo.repository;

import com.backend.acervo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTokenRecuperacao(String token);
    Optional<Usuario> findByRg(String rg);
    List<Usuario> findByAtivoTrue();
    List<Usuario> findByPapel(Usuario.Papel papel);
    
    @Query("SELECT u FROM Usuario u WHERE u.nome LIKE %:nome%")
    List<Usuario> buscarPorNomeContendo(@Param("nome") String nome);
    
    boolean existsByEmail(String email);
    boolean existsByRg(String rg);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<Usuario> findByAtivoFalse();
}