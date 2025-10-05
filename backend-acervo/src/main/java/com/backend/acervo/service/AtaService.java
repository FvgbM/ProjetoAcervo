package com.backend.acervo.service;

import org.springframework.stereotype.Service;
import com.backend.acervo.Ata;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.AtaRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AtaService {

    private final AtaRepository ataRepository;

    public AtaService(AtaRepository ataRepository) {
        this.ataRepository = ataRepository;
    }

    @Transactional
    public Ata criarAta(Ata novaAta, Usuario usuarioLogado) {
        validarCamposObrigatorios(novaAta); // (RN018)
        
        Integer proximoNumero = gerarProximoNumeroAta(); // (RF011 e RN016)
        novaAta.setNumeroAta(proximoNumero);
        novaAta.setCriadoPor(usuarioLogado);
        novaAta.setStatus(Ata.StatusAta.ATIVA);
        return ataRepository.save(novaAta);
    }

    private Integer gerarProximoNumeroAta() {
        Optional<Integer> maxNumeroOpt = ataRepository.findMaxNumeroAta();
        return maxNumeroOpt.map(num -> num + 1).orElse(1);        
    }
    
    private void validarCamposObrigatorios(Ata ata) {
        if (ata.getDataReuniao() == null) {
            throw new IllegalArgumentException("A data da reunião é obrigatória.");
        }
        if (ata.getDataReuniao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da reunião não pode ser futura.");
        }
        if (ata.getPauta() == null || ata.getPauta().trim().isEmpty()) {
            throw new IllegalArgumentException("A pauta é obrigatória.");
        }
        if (ata.getPauta().trim().length() < 10) {
            throw new IllegalArgumentException("A pauta deve ter no mínimo 10 caracteres."); // RN018
        }
    }

    public Optional<Ata> buscarPorId(Long id) {
        Optional<Ata> ataOpt = ataRepository.findById(id);
        // Retorna apenas atas ativas (soft delete)
        if (ataOpt.isPresent() && ataOpt.get().getStatus() == Ata.StatusAta.ATIVA) {
            return ataOpt;
        }
        return Optional.empty();
    }

    public List<Ata> listarAtasAtivas() {
        return ataRepository.findByStatusOrderByDataReuniaoDesc(Ata.StatusAta.ATIVA);
    }

    public List<Ata> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return ataRepository.findByDataReuniaoBetweenAndStatus(dataInicio, dataFim, Ata.StatusAta.ATIVA);
    }

    @Transactional
    public Ata atualizarAta(Long id, Ata ataAtualizada, Usuario usuarioLogado) {
        Ata ataExistente = ataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ata não encontrada!"));
        
        if (ataExistente.getStatus() != Ata.StatusAta.ATIVA) {
            throw new IllegalArgumentException("Apenas atas ativas podem ser atualizadas.");
        }
        
        // Validar os novos dados antes de atualizar
        validarCamposObrigatorios(ataAtualizada);
        
        // Atualizar apenas campos permitidos (nunca o número da ata - RN016)
        ataExistente.setDataReuniao(ataAtualizada.getDataReuniao());
        ataExistente.setPauta(ataAtualizada.getPauta());
        ataExistente.setParticipantes(ataAtualizada.getParticipantes());
        
        return ataRepository.save(ataExistente);
    }

    // soft delete
    @Transactional
    public void excluirAta(Long id, Usuario usuarioLogado) {
        Ata ata = ataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ata não encontrada!"));
        if (ata.getStatus() == Ata.StatusAta.EXCLUIDA) {
            throw new IllegalArgumentException("Ata já está excluída.");
        }
        ata.setStatus(Ata.StatusAta.EXCLUIDA);
        ataRepository.save(ata);
        // todo RN020 - notificacao para ADMIN
    }
}