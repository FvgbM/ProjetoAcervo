package com.backend.acervo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AtaDTO {
    @NotNull(message = "A data da reunião é obrigatória.")
    private LocalDate dataReuniao;

    @NotBlank(message = "A pauta é obrigatória.")
    @Size(min = 10, message = "A pauta deve ter no mínimo 10 caracteres.")
    private String pauta;

    private String participantes;

    public AtaDTO() {}

    public AtaDTO(LocalDate dataReuniao, String pauta, String participantes) {
        this.dataReuniao = dataReuniao;
        this.pauta = pauta;
        this.participantes = participantes;
    }

    public LocalDate getDataReuniao() { 
        return dataReuniao;
    }

    public String getPauta() {
        return pauta;
    }

    public void setPauta(String pauta) {
        this.pauta = pauta;
    }


    public void setDataReuniao(LocalDate dataReuniao) {
        this.dataReuniao = dataReuniao;
    }

    public String getParticipantes() {
        return participantes;
    }

    public void setParticipantes(String participantes) {
        this.participantes = participantes;
    }
}
