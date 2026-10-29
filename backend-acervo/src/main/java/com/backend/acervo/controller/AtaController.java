package com.backend.acervo.controller;

import com.backend.acervo.Ata;
import com.backend.acervo.Usuario;
import com.backend.acervo.dto.AtaDTO;
import com.backend.acervo.dto.AtaRespostaDTO;
import com.backend.acervo.repository.UsuarioRepository;
import com.backend.acervo.service.AtaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atas")
public class AtaController {

    private final AtaService ataService;
    private final UsuarioRepository usuarioRepository;

    public AtaController(AtaService ataService, UsuarioRepository usuarioRepository) {
        this.ataService = ataService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN')")
    public ResponseEntity<AtaRespostaDTO> criarAta(
            @Valid @RequestBody AtaDTO dto,
            Authentication authentication) {
        Ata ata = ataService.criarAta(paraEntidade(dto), usuarioLogado(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(AtaRespostaDTO.de(ata));
    }

    @GetMapping
    public ResponseEntity<List<AtaRespostaDTO>> listarTodasAtas(Authentication authentication) {
        List<AtaRespostaDTO> resposta = ataService.listarAcessiveis(usuarioLogado(authentication))
                .stream().map(AtaRespostaDTO::de).toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtaRespostaDTO> buscarAtaPorId(@PathVariable Long id, Authentication authentication) {
        return ataService.buscarAcessivel(id, usuarioLogado(authentication))
                .map(AtaRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<AtaRespostaDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Authentication authentication) {
        List<AtaRespostaDTO> resposta = ataService.buscarPorPeriodo(inicio, fim, usuarioLogado(authentication))
                .stream().map(AtaRespostaDTO::de).toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<AtaRespostaDTO>> buscarPorPauta(
            @RequestParam String trechoPauta, Authentication authentication) {
        List<AtaRespostaDTO> resposta = ataService.buscarPorPauta(trechoPauta, usuarioLogado(authentication))
                .stream().map(AtaRespostaDTO::de).toList();
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN')")
    public ResponseEntity<AtaRespostaDTO> atualizarAta(
            @PathVariable Long id, @Valid @RequestBody AtaDTO dto, Authentication authentication) {
        Ata ata = ataService.atualizar(id, paraEntidade(dto), usuarioLogado(authentication));
        return ResponseEntity.ok(AtaRespostaDTO.de(ata));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirAta(@PathVariable Long id, Authentication authentication) {
        ataService.excluir(id, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/liberacao-alunos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AtaRespostaDTO> definirLiberacao(
            @PathVariable Long id, @RequestParam boolean liberada, Authentication authentication) {
        Ata ata = ataService.definirLiberacaoAlunos(id, liberada, usuarioLogado(authentication));
        return ResponseEntity.ok(AtaRespostaDTO.de(ata));
    }

    private Usuario usuarioLogado(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private Ata paraEntidade(AtaDTO dto) {
        Ata ata = new Ata();
        ata.setDataReuniao(dto.getDataReuniao());
        ata.setPauta(dto.getPauta());
        ata.setParticipantes(dto.getParticipantes());
        return ata;
    }
}