package com.backend.acervo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.backend.acervo.Ata;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.AtaRepository;

@Service
public class AtaService {
    private final AtaRepository ataRepository;

    public AtaService(AtaRepository ataRepository) {
        this.ataRepository = Objects.requireNonNull(ataRepository, "AtaRepository é obrigatório");
    }

    @Transactional
    public Ata criarAta(Ata ata, Usuario usuario) {
        Ata ataObrigatoria = Objects.requireNonNull(ata, "Ata é obrigatória");
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        validar(ataObrigatoria);
        ataObrigatoria.setNumeroAta(ataRepository.findMaxNumeroAta().map(numero -> numero + 1).orElse(1));
        ataObrigatoria.setCriadoPor(usuarioObrigatorio);
        ataObrigatoria.setStatus(Ata.StatusAta.ATIVA);
        ataObrigatoria.setLiberadaParaAlunos(false);
        return ataRepository.save(ataObrigatoria);
    }

    @Transactional(readOnly = true)
    public List<Ata> listarAcessiveis(Usuario usuario) {
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        return ataRepository.findByStatusOrderByDataReuniaoDesc(Ata.StatusAta.ATIVA).stream()
                .filter(ata -> podeAcessar(ata, usuarioObrigatorio)).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Ata> buscarAcessivel(Long id, Usuario usuario) {
        Long idObrigatorio = Objects.requireNonNull(id, "ID da ata é obrigatório");
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        return ataRepository.findById(idObrigatorio).filter(ata -> ata.getStatus() == Ata.StatusAta.ATIVA)
                .filter(ata -> podeAcessar(ata, usuarioObrigatorio));
    }

    @Transactional(readOnly = true)
    public List<Ata> buscarPorPeriodo(LocalDate inicio, LocalDate fim, Usuario usuario) {
        LocalDate inicioObrigatorio = Objects.requireNonNull(inicio, "Data inicial é obrigatória");
        LocalDate fimObrigatorio = Objects.requireNonNull(fim, "Data final é obrigatória");
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (inicioObrigatorio.isAfter(fimObrigatorio)) throw new IllegalArgumentException("Período inválido");
        return ataRepository.findByDataReuniaoBetweenAndStatus(inicioObrigatorio, fimObrigatorio, Ata.StatusAta.ATIVA).stream()
                .filter(ata -> podeAcessar(ata, usuarioObrigatorio)).toList();
    }

    @Transactional(readOnly = true)
    public List<Ata> buscarPorPauta(String pauta, Usuario usuario) {
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (pauta == null || pauta.isBlank()) return List.of();
        return ataRepository.findByPautaContainingIgnoreCaseAndStatus(pauta.trim(), Ata.StatusAta.ATIVA).stream()
                .filter(ata -> podeAcessar(ata, usuarioObrigatorio)).toList();
    }

    @Transactional
    public Ata atualizar(Long id, Ata dados, Usuario usuario) {
        Ata ata = buscarParaGestao(id, usuario);
        Ata dadosObrigatorios = Objects.requireNonNull(dados, "Dados da ata são obrigatórios");
        validar(dadosObrigatorios);
        ata.setDataReuniao(dadosObrigatorios.getDataReuniao());
        ata.setPauta(dadosObrigatorios.getPauta().trim());
        ata.setParticipantes(dadosObrigatorios.getParticipantes());
        return ataRepository.save(ata);
    }

    @Transactional
    public void excluir(Long id, Usuario usuario) {
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (usuarioObrigatorio.getPerfil() != Usuario.Perfil.ADMIN) throw new SecurityException("Apenas administradores podem excluir atas");
        Long idObrigatorio = Objects.requireNonNull(id, "ID da ata é obrigatório");
        Ata ata = ataRepository.findById(idObrigatorio).orElseThrow(() -> new IllegalArgumentException("Ata não encontrada"));
        ata.setStatus(Ata.StatusAta.EXCLUIDA);
        ataRepository.save(ata);
    }

    @Transactional
    public Ata definirLiberacaoAlunos(Long id, boolean liberada, Usuario usuario) {
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (usuarioObrigatorio.getPerfil() != Usuario.Perfil.ADMIN) throw new SecurityException("Apenas administradores podem liberar atas");
        Long idObrigatorio = Objects.requireNonNull(id, "ID da ata é obrigatório");
        Ata ata = ataRepository.findById(idObrigatorio).orElseThrow(() -> new IllegalArgumentException("Ata não encontrada"));
        ata.setLiberadaParaAlunos(liberada);
        return ataRepository.save(ata);
    }

    public boolean podeAcessar(Ata ata, Usuario usuario) {
        Ata ataObrigatoria = Objects.requireNonNull(ata, "Ata é obrigatória");
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (ataObrigatoria.getStatus() != Ata.StatusAta.ATIVA) return false;
        return switch (usuarioObrigatorio.getPerfil()) {
            case ADMIN, COLABORADOR, PROFESSOR -> true;
            case ALUNO -> ataObrigatoria.isLiberadaParaAlunos()
                    && !ataObrigatoria.getDataReuniao().isAfter(LocalDate.now().minusDays(10));
        };
    }

    private Ata buscarParaGestao(Long id, Usuario usuario) {
        Long idObrigatorio = Objects.requireNonNull(id, "ID da ata é obrigatório");
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuário é obrigatório");
        if (usuarioObrigatorio.getPerfil() != Usuario.Perfil.ADMIN && usuarioObrigatorio.getPerfil() != Usuario.Perfil.COLABORADOR) {
            throw new SecurityException("Sem permissão para gerenciar atas");
        }
        return ataRepository.findById(idObrigatorio).filter(ata -> ata.getStatus() == Ata.StatusAta.ATIVA)
                .orElseThrow(() -> new IllegalArgumentException("Ata não encontrada"));
    }

    private void validar(Ata ata) {
        if (ata.getDataReuniao() == null || ata.getDataReuniao().isAfter(LocalDate.now())) throw new IllegalArgumentException("Data da reunião inválida");
        if (ata.getPauta() == null || ata.getPauta().trim().length() < 10) throw new IllegalArgumentException("A pauta deve ter no mínimo 10 caracteres");
    }
}