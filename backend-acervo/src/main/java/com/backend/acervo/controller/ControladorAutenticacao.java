package com.backend.acervo.controller;

import com.backend.acervo.dto.*;
import com.backend.acervo.RequisicaoLogin;
import com.backend.acervo.Usuario;
import com.backend.acervo.service.AutenticacaoService;
import com.backend.acervo.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/autenticacao")
public class ControladorAutenticacao {

    private final AuthenticationManager gerenciadorAutenticacao;
    private final AutenticacaoService autenticacaoService;
    private final UsuarioRepository usuarioRepository;

    public ControladorAutenticacao(
            AuthenticationManager gerenciadorAutenticacao,
            PasswordEncoder codificadorSenha,
            AutenticacaoService autenticacaoService,
            UsuarioRepository usuarioRepository) {
        this.gerenciadorAutenticacao = gerenciadorAutenticacao;
        this.autenticacaoService = autenticacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequisicaoLogin requisicao) {
        System.out.println("Dados recebidos - Email: '" + requisicao.email() + "', Senha: '" + requisicao.senha() + "'");
    
    try {
                
        Authentication autenticacao = gerenciadorAutenticacao.authenticate(
            new UsernamePasswordAuthenticationToken(requisicao.email(), requisicao.senha())
        );
        
        System.out.println("Autenticação bem-sucedida!");
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
        
    
        Usuario usuario = usuarioRepository.findByEmail(requisicao.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado após autenticação"));
        
        
        System.out.println("Usuário autenticado: " + usuario.getEmail());
        System.out.println("Papel: " + usuario.getPapel());
        System.out.println("Nome: " + usuario.getNome());
        System.out.println("Primeiro login: " + usuario.isPrimeiroLogin());
        
        if (usuario.isPrimeiroLogin()) {
            return ResponseEntity.ok(Map.of(
                "primeiroLogin", true,
                "mensagem", "Você deve alterar sua senha no primeiro acesso",
                "email", usuario.getEmail(),
                "nome", usuario.getNome(),
                "papel", usuario.getPapel().name()
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "mensagem", "Login realizado com sucesso",
            "papel", usuario.getPapel().name(),
            "nome", usuario.getNome(),
            "email", usuario.getEmail(),
            "primeiroLogin", false
        ));
        
    } catch (AuthenticationException e) {
        System.out.println("ERRO DE AUTENTICAÇÃO: " + e.getClass().getSimpleName());
        System.out.println("Mensagem: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(401).body(Map.of("erro", "Credenciais inválidas"));
    }
}

    @PostMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(@RequestBody RequisicaoAlterarSenha requisicao) {
        try {
            return ResponseEntity.ok(autenticacaoService.alterarSenha(
                requisicao.email(),
                requisicao.senhaAtual(),
                requisicao.novaSenha()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "erro", e.getMessage()
            ));
        }
    }

    @PostMapping("/solicitar-recuperacao")
    public ResponseEntity<?> solicitarRecuperacaoSenha(@RequestBody SolicitarRecuperacaoRequest request) {
        try {
            autenticacaoService.solicitarRecuperacaoSenha(request.email());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "erro", e.getMessage()
            ));
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
        try {
            autenticacaoService.redefinirSenha(
                request.token(), 
                request.novaSenha()
            );
            return ResponseEntity.ok(Map.of(
                "mensagem", "Senha redefinida com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "erro", e.getMessage()
            ));
        }
    }
}