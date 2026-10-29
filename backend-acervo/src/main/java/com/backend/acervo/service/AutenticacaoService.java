package com.backend.acervo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.acervo.TokenRecuperacaoSenha;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.TokenRecuperacaoSenhaRepository;
import com.backend.acervo.repository.UsuarioRepository;

@Service
public class AutenticacaoService {

    private static final int MAXIMO_TENTATIVAS = 3;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final int HORAS_EXPIRACAO_TOKEN = 2;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final PasswordEncoder codificadorSenha;
    private final EmailService emailService;
    private final String frontendBaseUrl;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            TokenRecuperacaoSenhaRepository tokenRepository,
            PasswordEncoder codificadorSenha,
            EmailService emailService,
            @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.codificadorSenha = codificadorSenha;
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void registrarLoginComSucesso(String email) {
        Usuario usuario = buscarUsuario(email);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarFalhaDeLogin(String email) {
        String emailNormalizado = normalizarEmail(email);
        usuarioRepository.findByEmail(emailNormalizado).ifPresent(usuario -> {
            if (!usuario.isAtivo()
                    || usuario.getStatusUsuario() != Usuario.StatusUsuario.ATIVO) {
                return;
            }

            int tentativas = usuario.getTentativasFalhas() + 1;
            usuario.setTentativasFalhas(tentativas);

            if (tentativas >= MAXIMO_TENTATIVAS) {
                usuario.setTentativasFalhas(0);
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
            }

            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void alterarSenha(String emailAutenticado, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarUsuario(emailAutenticado);

        if (!usuario.isAtivo() || usuario.getStatusUsuario() != Usuario.StatusUsuario.ATIVO) {
            throw new IllegalStateException("Conta indisponível");
        }
        if (!codificadorSenha.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual inválida");
        }

        validarSenha(novaSenha);
        if (codificadorSenha.matches(novaSenha, usuario.getSenha())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual");
        }

        usuario.setSenha(codificadorSenha.encode(novaSenha));
        usuario.setPrimeiroLogin(false);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        tokenRepository.invalidarTokensAtivosDoUsuario(usuario.getId(), LocalDateTime.now());
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        String emailNormalizado = normalizarEmail(email);
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);

        // Não revelar se uma conta existe, está inativa ou foi bloqueada.
        if (usuario == null
                || !usuario.isAtivo()
                || usuario.getStatusUsuario() != Usuario.StatusUsuario.ATIVO) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        tokenRepository.invalidarTokensAtivosDoUsuario(usuario.getId(), agora);

        String tokenPuro = gerarTokenSeguro();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUsuario(usuario);
        token.setTokenHash(calcularHash(tokenPuro));
        token.setCriadoEm(agora);
        token.setExpiraEm(agora.plusHours(HORAS_EXPIRACAO_TOKEN));
        tokenRepository.save(token);

        String link = frontendBaseUrl + "/recuperar-senha?token=" + tokenPuro;
        emailService.enviarEmailRecuperacao(usuario.getEmail(), link);
    }

    @Transactional
    public void redefinirSenha(String tokenPuro, String novaSenha) {
        String tokenHash = calcularHash(tokenPuro);
        TokenRecuperacaoSenha token = tokenRepository
                .findFirstByTokenHashAndUsadoEmIsNull(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        LocalDateTime agora = LocalDateTime.now();
        if (!token.getExpiraEm().isAfter(agora)) {
            token.setUsadoEm(agora);
            tokenRepository.save(token);
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        Usuario usuario = token.getUsuario();
        if (!usuario.isAtivo() || usuario.getStatusUsuario() != Usuario.StatusUsuario.ATIVO) {
            throw new IllegalStateException("Conta indisponível");
        }

        validarSenha(novaSenha);
        if (codificadorSenha.matches(novaSenha, usuario.getSenha())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual");
        }

        usuario.setSenha(codificadorSenha.encode(novaSenha));
        usuario.setPrimeiroLogin(false);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        tokenRepository.invalidarTokensAtivosDoUsuario(usuario.getId(), agora);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail obrigatório");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String gerarTokenSeguro() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String calcularHash(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || !senha.matches(
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{12,}$")) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo 12 caracteres, uma letra maiúscula, "
                            + "uma minúscula, um número e um caractere especial"
            );
        }
    }
}