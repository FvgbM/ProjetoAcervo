package com.backend.acervo.controller;

import com.backend.acervo.Usuario;
import com.backend.acervo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<?> aprovarUsuario(
            @PathVariable Long id,
            @RequestBody AprovarUsuarioRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        try {
            if (id.equals(usuarioLogado.getId())) {
                return ResponseEntity.badRequest()
                        .body("Não é possível aprovar o próprio usuário.");
            }

            Usuario usuarioAprovado = usuarioService.aprovarUsuario(id, request.getPerfil());
            return ResponseEntity.ok(usuarioAprovado);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro interno ao aprovar usuário: " + e.getMessage());
        }
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<List<Usuario>> listarPendentes() {
        List<Usuario> pendentes = usuarioService.listarPorStatus("PENDENTE");
        return ResponseEntity.ok(pendentes);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<List<Usuario>> listarUsuarios(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Usuario.Perfil perfil,
            @RequestParam(required = false) String nome) {
        
        List<Usuario> usuarios;
        
        if (status != null) {
            usuarios = usuarioService.listarPorStatus(status);
        } else if (perfil != null) {
            usuarios = usuarioService.findByPerfil(perfil);
        } else if (nome != null && !nome.trim().isEmpty()) {
            usuarios = usuarioService.buscarPorNome(nome.trim());
        } else {
            usuarios = usuarioService.listarTodos();
        }
        
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarUsuarioPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        if (!usuarioLogado.getPerfil().equals(Usuario.Perfil.COLABORADOR) && 
            !usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado.");
        }

        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/perfil")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<?> atualizarPerfil(
            @PathVariable Long id,
            @RequestBody AtualizarPerfilRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        try {
            if (id.equals(usuarioLogado.getId())) {
                return ResponseEntity.badRequest()
                        .body("Não é possível alterar o próprio perfil.");
            }

            Usuario usuario = usuarioService.atualizarPerfil(id, request.getPerfil());
            return ResponseEntity.ok(usuario);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizarStatusRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        try {
            if (id.equals(usuarioLogado.getId())) {
                return ResponseEntity.badRequest()
                        .body("Não é possível alterar o próprio status.");
            }

            Usuario usuario = usuarioService.atualizarStatus(id, request.getStatus());
            return ResponseEntity.ok(usuario);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<?> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuarioAtualizado,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        try {
            if (id.equals(usuarioLogado.getId())) {
                return ResponseEntity.badRequest()
                        .body("Não é possível alterar o próprio usuário por este endpoint.");
            }

            Usuario usuario = usuarioService.atualizarUsuario(id, usuarioAtualizado);
            return ResponseEntity.ok(usuario);
            
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<?> deletarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        try {
            if (id.equals(usuarioLogado.getId())) {
                return ResponseEntity.badRequest()
                        .body("Não é possível desativar o próprio usuário.");
            }

            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok().body("Usuário desativado com sucesso.");
            
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<List<Usuario>> listarUsuariosAtivos() {
        List<Usuario> ativos = usuarioService.listarUsuariosAtivos();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasAuthority('COLABORADOR')")
    public ResponseEntity<List<Usuario>> listarUsuariosInativos() {
        List<Usuario> inativos = usuarioService.findByAtivoFalse();
        return ResponseEntity.ok(inativos);
    }

    public static class AprovarUsuarioRequest {
        private Usuario.Perfil perfil;

        public Usuario.Perfil getPerfil() { 
        return perfil;
        }

        public void setPerfil(Usuario.Perfil perfil) { 
            this.perfil = perfil;
        }
    }

    public static class AtualizarPerfilRequest {
        private Usuario.Perfil perfil;

        public Usuario.Perfil getPerfil() {
            return perfil; 
    }
        public void setPerfil(Usuario.Perfil perfil) { 
            this.perfil = perfil;
        }
    }

    public static class AtualizarStatusRequest {
        private String status;

        public String getStatus() { 
        return status; 
    }
        public void setStatus(String status) {
        this.status = status;
        }
    }
}