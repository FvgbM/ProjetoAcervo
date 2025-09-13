package com.backend.acervo.controller;

import com.backend.acervo.Arquivo;
import com.backend.acervo.Usuario;
import com.backend.acervo.service.ArquivoService;
import com.backend.acervo.exceptions.ArquivoNaoEncontradoException;
import com.backend.acervo.exceptions.UsuarioNaoEncontradoException;
import com.backend.acervo.repository.UsuarioRepository;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/arquivos")
public class ArquivoController {

    private final ArquivoService arquivoService;
    private final UsuarioRepository usuarioRepository;

    public ArquivoController(ArquivoService arquivoService, UsuarioRepository usuarioRepository) {
        this.arquivoService = arquivoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/upload")
public ResponseEntity<?> uploadArquivo(@RequestParam MultipartFile file) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        
        Arquivo arquivoSalvo = arquivoService.salvarArquivo(file, usuario);
        return ResponseEntity.ok(arquivoSalvo);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Erro ao processar arquivo");
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long id) {
        try {
            Resource resource = arquivoService.carregarArquivo(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (ArquivoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}


