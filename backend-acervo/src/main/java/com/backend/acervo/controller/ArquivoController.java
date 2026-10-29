package com.backend.acervo.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.backend.acervo.Usuario;
import com.backend.acervo.dto.ArquivoRespostaDTO;
import com.backend.acervo.repository.UsuarioRepository;
import com.backend.acervo.service.ArquivoService;

@RestController
@RequestMapping("/api/arquivos")
public class ArquivoController {
    private final ArquivoService service; private final UsuarioRepository usuarios;
    public ArquivoController(ArquivoService service, UsuarioRepository usuarios) { this.service = service; this.usuarios = usuarios; }
    private Usuario usuario(Authentication auth) { return usuarios.findByEmail(auth.getName()).orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado")); }

    @PostMapping("/validar") @PreAuthorize("hasAnyRole('COLABORADOR','ADMIN')")
    public Map<String, Object> validar(@RequestParam("file") MultipartFile file) {
        service.validar(file); return Map.of("valido", true, "tamanho", file.getSize(), "tipo", "application/pdf");
    }

    @PostMapping(value="/upload/{ataId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('COLABORADOR','ADMIN')")
    public ResponseEntity<ArquivoRespostaDTO> upload(@PathVariable Long ataId, @RequestParam("file") MultipartFile file, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ArquivoRespostaDTO.de(service.salvarParaAta(ataId, file, usuario(auth))));
    }

    @GetMapping("/download/{ataId}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long ataId, Authentication auth) {
        ByteArrayResource recurso = service.baixar(ataId, usuario(auth));
        return ResponseEntity.ok().contentType(Objects.requireNonNull(MediaType.APPLICATION_PDF,
        "Tipo de conteúdo PDF indisponível"))
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ata-" + ataId + ".pdf")
          .header("X-Content-Type-Options", "nosniff").body(recurso);
    }
}