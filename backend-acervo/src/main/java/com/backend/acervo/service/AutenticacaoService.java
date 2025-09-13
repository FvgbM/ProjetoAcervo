package com.backend.acervo.service;

import com.backend.acervo.exceptions.*;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AutenticacaoService {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoService.class);
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorSenha;
    private final EmailService emailService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder codificadorSenha,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorSenha = codificadorSenha;
        this.emailService = emailService;
        criarUsuarioAdmin();
    }
    
    private void criarUsuarioAdmin() {
        if (usuarioRepository.findByEmail("admin@fatec.sp.gov.br").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@fatec.sp.gov.br");
            admin.setSenha(codificadorSenha.encode("@Admin123"));
            admin.setNome("Administrador");
            admin.setRg("ADMIN.RG.SP");
            admin.setPapel(Usuario.Papel.COLABORADOR);
            admin.setPrimeiroLogin(true);
            usuarioRepository.save(admin);
        }
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String senha) {
        logger.info("Tentativa de autenticação para o email: {}", email);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.warn("Usuário não encontrado com email: {}", email);
                return new UsuarioNaoEncontradoException("Usuário não encontrado");
            });
        
        if (!usuario.isAtivo()) {
            logger.warn("Tentativa de login para usuário inativo: {}", email);
            throw new UsuarioInativoException("Conta desativada");
        }
        
        if (!codificadorSenha.matches(senha, usuario.getSenha())) {
            logger.warn("Senha incorreta para o usuário: {}", email);
            throw new SenhaIncorretaException("Senha incorreta");
        }
        
        logger.info("Autenticação bem-sucedida para: {}", email);
        return usuario;
    }

    

    @Transactional
    public String alterarSenha(String email, String senhaAtual, String novaSenha) {
        logger.info("Solicitação de alteração de senha para: {}", email);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        
        if (!codificadorSenha.matches(senhaAtual, usuario.getSenha())) {
            throw new SenhaIncorretaException("Senha atual incorreta");
        }
        
        validarSenha(novaSenha);
        
        usuario.setSenha(codificadorSenha.encode(novaSenha));
        usuario.setPrimeiroLogin(false);
        usuarioRepository.save(usuario);
        
        logger.info("Senha alterada com sucesso para: {}", email);
        return "Senha alterada com sucesso";
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        logger.info("Solicitação de recuperação de senha para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException("Conta desativada");
        }
        
        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(2));
        usuarioRepository.save(usuario);
        
        String linkRecuperacao = "http://seu-site.com/recuperar-senha?token=" + token;
        emailService.enviarEmailRecuperacao(email, linkRecuperacao);
        
        logger.info("Email de recuperação enviado para: {}", email);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        logger.info("Tentativa de redefinição de senha com token: {}", token);

        Usuario usuario = usuarioRepository.findByTokenRecuperacao(token)
            .orElseThrow(() -> new TokenInvalidoException("Token inválido ou expirado"));

        if (usuario.getDataExpiracaoToken().isBefore(LocalDateTime.now())) {
            throw new TokenExpiradoException("Token expirado");
        }
        
        validarSenha(novaSenha);
        
        usuario.setSenha(codificadorSenha.encode(novaSenha));
        usuario.setTokenRecuperacao(null);
        usuario.setDataExpiracaoToken(null);
        usuarioRepository.save(usuario);
        
        logger.info("Senha redefinida com sucesso para o usuário: {}", usuario.getEmail());
    }

    private void validarSenha(String senha) {
        if (!senhaValida(senha)) {
            throw new SenhaInvalidaException(
                "A senha deve conter pelo menos: " +
                "1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial (@#$%^&+=)");
        }
    }

    private boolean senhaValida(String senha) {
        String padrao = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return senha.matches(padrao);
    }
}