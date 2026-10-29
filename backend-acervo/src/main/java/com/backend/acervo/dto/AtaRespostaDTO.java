package com.backend.acervo.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.backend.acervo.Ata;


public record AtaRespostaDTO(
    Long id, Integer numeroAta, LocalDate dataReuniao, String pauta,
    String participantes, String status, boolean liberadaParaAlunos,
    LocalDateTime criadoEm, boolean possuiArquivo
) {
    public static AtaRespostaDTO de(Ata ata) {
        return new AtaRespostaDTO(ata.getId(), ata.getNumeroAta(), ata.getDataReuniao(),
            ata.getPauta(), ata.getParticipantes(), ata.getStatus().name(),
            ata.isLiberadaParaAlunos(), ata.getCriadoEm(), ata.getArquivo() != null);
    }
}