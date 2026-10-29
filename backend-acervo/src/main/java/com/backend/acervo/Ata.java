package com.backend.acervo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "atas")
public class Ata {
    public enum StatusAta {
        RASCUNHO,
        ATIVA,
        EXCLUIDA  // Soft Delete - Ata não é mais listada, mas permanece no BD (RN021)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número sequencial único (RN016)
    @Column(nullable = false, unique = true)
    private Integer numeroAta;

    @Column(nullable = false)
    private LocalDate dataReuniao;

    @Column(nullable = false)
    private boolean liberadaParaAlunos = false;

    public boolean isLiberadaParaAlunos() { return liberadaParaAlunos; }
    public void setLiberadaParaAlunos(boolean valor) { this.liberadaParaAlunos = valor; }

    // Pauta da reunião (metadado obrigatório, mínimo 10 caracteres - RN018)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String pauta;

    @Column(columnDefinition = "TEXT")
    private String participantes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario criadoPor;

    @OneToOne(mappedBy = "ata", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Arquivo arquivo;

    // Data de criação do registro no sistema
    @CreationTimestamp
    private LocalDateTime criadoEm;

    // Soft Delete
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAta status = StatusAta.ATIVA;

    public Ata() {}

    public Ata(Integer numeroAta, LocalDate dataReuniao, String pauta, Usuario criadoPor) {
        this.numeroAta = numeroAta;
        this.dataReuniao = dataReuniao;
        this.pauta = pauta;
        this.criadoPor = criadoPor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroAta() {
        return numeroAta;
    }

    public void setNumeroAta(Integer numeroAta) {
        this.numeroAta = numeroAta;
    }

    public LocalDate getDataReuniao() {
        return dataReuniao;
    }

    public void setDataReuniao(LocalDate dataReuniao) {
        this.dataReuniao = dataReuniao;
    }

    public String getPauta() {
        return pauta;
    }

    public void setPauta(String pauta) {
        this.pauta = pauta;
    }

    public String getParticipantes() {
        return participantes;
    }

    public void setParticipantes(String participantes) {
        this.participantes = participantes;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public StatusAta getStatus() {
        return status;
    }

    public void setStatus(StatusAta status) {
        this.status = status;
    }

    public Arquivo getArquivo() {
        return arquivo;
    }

    public void setArquivo(Arquivo arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public String toString() {
        return "Ata{" +
                "id=" + id +
                ", numeroAta=" + numeroAta +
                ", dataReuniao=" + dataReuniao +
                ", pauta='" + pauta + '\'' +
                ", status=" + status +
                '}';
    }
}