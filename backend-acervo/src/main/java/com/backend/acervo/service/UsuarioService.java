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
        usuario.setStatus("PENDENTE");
                
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        emailService.enviarEmailAcesso(usuarioSalvo.getEmail(), senhaTemp);

        return usuarioSalvo;
    }

    @Transactional
    public Usuario aprovarUsuario(Long usuarioId, Usuario.Perfil perfil) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId));
        
            if (!"PENDENTE".equals(usuario.getStatus())) {
                throw new IllegalStateException("Apenas usuários pendentes podem ser aprovados.");
            }
            if (perfil == null) {
                throw new IllegalArgumentException("O perfil do usuário não pode ser nulo.");
            }
            usuario.setPerfil(perfil);
            usuario.setStatus("ATIVO");
            return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id)
            .map(usuario -> {
                usuario.setNome(usuarioAtualizado.getNome());
                usuario.setEmail(usuarioAtualizado.getEmail());
                usuario.setRg(usuarioAtualizado.getRg());
                usuario.setPerfil(usuarioAtualizado.getPerfil());
                usuario.setAtivo(usuarioAtualizado.isAtivo());
                usuario.setStatus(usuarioAtualizado.getStatus());
                return usuarioRepository.save(usuario);
            })
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));

            usuario.setAtivo(false);
            usuario.setStatus("INATIVO");
            usuarioRepository.save(usuario);
        
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorStatus(String status) {
        return usuarioRepository.findByStatus(status);
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

    public List<Usuario> findByPerfil(Usuario.Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public Usuario atualizarPerfil(Long usuarioId, Usuario.Perfil novoPerfil) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId));

        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarStatus(Long usuarioId, String novoStatus) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId));

        usuario.setStatus(novoStatus);

        if ("ATIVO".equals(novoStatus)) {
            usuario.setAtivo(true);
        } else if ("INATIVO".equals(novoStatus) || "BLOQUEADO".equals(novoStatus)) {
            usuario.setAtivo(false);
        }
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listaPorPerfil(Usuario.Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }

    public List<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    public boolean podeSerAprovado(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (!"PENDENTE".equals(usuario.getStatus())) {
            return false;
        }
        String email = usuario.getEmail();
        return email != null && email.endsWith("@fatec.sp.gov.br");
    }

    public boolean existeUsuarioAtivoComEmail(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        return usuarioOpt.isPresent() && "ATIVO".equals(usuarioOpt.get().getStatus());
    }
}