package com.backend.acervo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.acervo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByRg(String rg);

    boolean existsByEmail(String email);
    boolean existsByRg(String rg);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByRgAndIdNot(String rg, Long id);

    List<Usuario> findByAtivoTrue();
    List<Usuario> findByAtivoFalse();
    List<Usuario> findByPerfil(Usuario.Perfil perfil);
    List<Usuario> findByStatusUsuario(Usuario.StatusUsuario statusUsuario);
    List<Usuario> findByNomeContainingIgnoreCase(String nome);
}