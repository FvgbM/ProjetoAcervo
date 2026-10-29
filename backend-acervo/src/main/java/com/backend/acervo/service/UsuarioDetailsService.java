package com.backend.acervo.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.acervo.Usuario;
import com.backend.acervo.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));

        boolean bloqueioTemporarioAtivo = usuario.getBloqueadoAte() != null
                && usuario.getBloqueadoAte().isAfter(LocalDateTime.now());

        boolean contaAtiva = usuario.isAtivo()
                && usuario.getStatusUsuario() == Usuario.StatusUsuario.ATIVO;

        String authority = usuario.getPerfil() == null
                ? "ROLE_PENDENTE"
                : "ROLE_" + usuario.getPerfil().name();

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(authority)
                .disabled(!contaAtiva)
                .accountLocked(bloqueioTemporarioAtivo
                        || usuario.getStatusUsuario() == Usuario.StatusUsuario.BLOQUEADO)
                .build();
    }
}