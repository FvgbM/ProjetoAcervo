package com.backend.acervo.controller;

import com.backend.acervo.Ata;
import com.backend.acervo.Usuario;
import com.backend.acervo.dto.AtaDTO;
import com.backend.acervo.service.AtaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/atas")
@CrossOrigin(origins = "http://localhost:4200")
public class AtaController {

    private final AtaService ataService;

    public AtaController(AtaService ataService) {
        this.ataService = ataService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COLABORADOR') or hasAuthority('ADMIN')") // Apenas colaboradores e administradores podem criar atas
    public ResponseEntity<?> criarAta(@Valid @RequestBody AtaDTO ataDTO, 
                                      @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            Ata novaAta = new Ata();
            novaAta.setDataReuniao(ataDTO.getDataReuniao());
            novaAta.setPauta(ataDTO.getPauta());
            novaAta.setParticipantes(ataDTO.getParticipantes());

            Ata ataCriada = ataService.criarAta(novaAta, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(ataCriada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar a ata: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Ata>> listarTodasAtas() {
        List<Ata> atas = ataService.listarAtasAtivas();
        return ResponseEntity.ok(atas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ata> buscarAtaPorId(@PathVariable Long id) {
        Optional<Ata> ata = ataService.buscarPorId(id);
        return ata.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<Ata>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        List<Ata> atas = ataService.listarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(atas);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<Ata>> buscarPorPauta(@RequestParam String trechoPauta) {

        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COLABORADOR') or hasAuthority('ADMIN')") // Apenas colaboradores e administradores podem atualizar
    public ResponseEntity<?> atualizarAta(@PathVariable Long id, 
                                         @Valid @RequestBody AtaDTO ataDTO,
                                         @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            Ata ataAtualizada = new Ata();
            ataAtualizada.setDataReuniao(ataDTO.getDataReuniao());
            ataAtualizada.setPauta(ataDTO.getPauta());
            ataAtualizada.setParticipantes(ataDTO.getParticipantes());

            Ata ata = ataService.atualizarAta(id, ataAtualizada, usuarioLogado);
            return ResponseEntity.ok(ata);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao atualizar a ata: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COLABORADOR') or hasAuthority('ADMIN')") // Apenas colaboradores e administradores podem excluir
    public ResponseEntity<?> excluirAta(@PathVariable Long id,
                                       @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ataService.excluirAta(id, usuarioLogado);
            return ResponseEntity.ok().body("Ata excluída com sucesso.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao excluir a ata: " + e.getMessage());
        }
    }        
}