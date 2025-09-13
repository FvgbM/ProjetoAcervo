package com.backend.acervo.service;

import com.backend.acervo.exceptions.UsuarioNaoEncontradoException;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    @Transactional
    public Usuario criaUsuario(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Email já está em uso.");
        }
        if (usuarioRepository.findByRg(usuario.getRg()).isPresent()) {
            throw new RuntimeException("RG já está em uso.");
        }

        String senhaTemp = UUID.randomUUID().toString().substring(0, 8);
        usuario.setSenha(passwordEncoder.encode(senhaTemp));
        usuario.setPrimeiroLogin(true);
        usuario.setAtivo(true);
                
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        emailService.enviarEmailAcesso(usuarioSalvo.getEmail(), senhaTemp);

        return usuarioSalvo;
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id)
            .map(usuario -> {
                usuario.setNome(usuarioAtualizado.getNome());
                usuario.setEmail(usuarioAtualizado.getEmail());
                usuario.setRg(usuarioAtualizado.getRg());
                usuario.setPapel(usuarioAtualizado.getPapel());
                usuario.setAtivo(usuarioAtualizado.isAtivo());
                return usuarioRepository.save(usuario);
            })
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));

            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
        
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));
    }

    public List<Usuario> listarUsuariosAtivos() {
        return usuarioRepository.findByAtivoTrue();
    } 

    public List<Usuario> findByAtivoFalse() {
        return usuarioRepository.findByAtivoFalse();
    }

    public Optional<Usuario> buscarEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarRg(String rg) {
        return usuarioRepository.findByRg(rg);
    }

    public Optional<Usuario> buscarTokenRecuperacao(String token) {
        return usuarioRepository.findByTokenRecuperacao(token);
    }

    public List<Usuario> findByPapel(Usuario.Papel papel) {
        return usuarioRepository.findByPapel(papel);
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}