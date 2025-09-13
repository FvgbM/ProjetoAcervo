package com.backend.acervo.service;

import com.backend.acervo.Usuario;
import com.backend.acervo.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicoDetalhesUsuario implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public ServicoDetalhesUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    System.out.println("Buscando usuário: '" + email + "'");
    
    System.out.println("Todos os usuários no banco:");
    usuarioRepository.findAll().forEach(usuario -> {
        System.out.println("   - Email: " + usuario.getEmail() + 
                         ", Senha: " + usuario.getSenha() +
                         ", Tamanho: " + usuario.getSenha().length());
    });
    
    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> {
                System.out.println("USUÁRIO NÃO ENCONTRADO NO BANCO: '" + email + "'");
                return new UsernameNotFoundException("Usuário não encontrado");
            });
    
    System.out.println("Usuário encontrado: " + usuario.getEmail());
    System.out.println("Hash da senha: " + usuario.getSenha());
    System.out.println("Tamanho do hash: " + usuario.getSenha().length());
    
    return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getSenha())
            .roles(usuario.getPapel().name())
            .build();
}
}