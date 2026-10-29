package com.backend.acervo.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.acervo.RequisicaoLogin;
import com.backend.acervo.Usuario;
import com.backend.acervo.dto.RequisicaoAlterarSenha;
import com.backend.acervo.dto.RedefinirSenhaRequest;
import com.backend.acervo.dto.SolicitarRecuperacaoRequest;
import com.backend.acervo.repository.UsuarioRepository;
import com.backend.acervo.service.AutenticacaoService;

@RestController
@RequestMapping("/api/autenticacao")
public class ControladorAutenticacao {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AutenticacaoService autenticacaoService;
    private final UsuarioRepository usuarioRepository;

    public ControladorAutenticacao(
            AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository,
            AutenticacaoService autenticacaoService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager; this.securityContextRepository = securityContextRepository;
        this.autenticacaoService = autenticacaoService; this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody RequisicaoLogin requisicao,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Authentication autenticacao = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            requisicao.email().trim().toLowerCase(),
                            requisicao.senha()
                    )
            );

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);
            securityContextRepository.saveContext(contexto, request, response);

            autenticacaoService.registrarLoginComSucesso(autenticacao.getName());
            Usuario usuario = usuarioRepository.findByEmail(autenticacao.getName())
                    .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Login realizado com sucesso",
                    "usuario", Map.of(
                    "id", usuario.getId(),
                    "nome", usuario.getNome(),
                    "email", usuario.getEmail(),
                    "perfil", usuario.getPerfil().name(),
                    "primeiroLogin", usuario.isPrimeiroLogin())
            ));
        } catch (AuthenticationException exception) {
            autenticacaoService.registrarFalhaDeLogin(requisicao.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas ou conta indisponível"));
        }
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(
            @Valid @RequestBody RequisicaoAlterarSenha requisicao,
            Authentication autenticacao) {
        try {
            autenticacaoService.alterarSenha(
                    autenticacao.getName(),
                    requisicao.senhaAtual(),
                    requisicao.novaSenha()
            );
            return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("erro", exception.getMessage()));
        }
    }

    @PostMapping("/solicitar-recuperacao")
    public ResponseEntity<?> solicitarRecuperacaoSenha(
            @Valid @RequestBody SolicitarRecuperacaoRequest request) {
        autenticacaoService.solicitarRecuperacaoSenha(request.email());
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se o e-mail estiver cadastrado, você receberá as instruções de recuperação."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request) {
        try {
            autenticacaoService.redefinirSenha(request.token(), request.novaSenha());
            return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso"));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Não foi possível redefinir a senha"));
        }
    }
}