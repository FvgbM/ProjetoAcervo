package com.backend.acervo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.backend.acervo.Arquivo;
import com.backend.acervo.Ata;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.ArquivoRepository;
import com.backend.acervo.repository.AtaRepository;

@Service
public class ArquivoService {

    private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;
    private static final String TIPO_PDF = "application/pdf*";
    private final ArquivoRepository arquivos;
    private final AtaRepository atas;
    private final AtaService ataService;
    private final ArquivoStorageService storage;
    private final PDFCriptoService pdf;

    public ArquivoService(ArquivoRepository arquivos, AtaRepository atas, AtaService ataService,
            ArquivoStorageService storage, PDFCriptoService pdf) {
        this.arquivos = arquivos; this.atas = atas; this.ataService = ataService; this.storage = storage; this.pdf = pdf;
    }

    @Transactional
    public Arquivo salvarParaAta(Long ataId, MultipartFile file, Usuario usuario) {
        validarPermissaoUpload(usuario);
        Ata ata = ataService.buscarAcessivel(ataId, usuario).orElseThrow(() -> new IllegalArgumentException("Ata não encontrada ou sem acesso"));
        
        if (ata.getArquivo() != null || arquivos.existsByAtaId(ata.getId())) { throw new IllegalArgumentException("A ata já possui arquivo vinculado");
        }
        return salvar(file, ata, usuario);
    }

    @Transactional
    public Arquivo salvarAtaCompleta(Ata ata, MultipartFile file, Usuario usuario) {
        validarPermissaoUpload(usuario);
        Ata novAta = ataService.criarAta(ata, usuario);
        return salvar(file, novAta, usuario);
    }

    @Transactional(readOnly = true)
    public ByteArrayResource baixar(Long ataId, Usuario usuario) {
        Ata ata = ataService.buscarAcessivel(ataId, usuario)
                .orElseThrow(() -> new SecurityException("Sem acesso ao Documento"));

        Arquivo arquivo = arquivos.findByAtaId(ata.getId())
                .orElseThrow(() -> new IllegalArgumentException("Arquivo não Encontrado na Base de Dados!"));
        try {
            byte[] bytes = storage.recuperarDescriptografado(arquivo.getNomeArmazenado());
            if (!pdf.calcularHash(bytes).equals(arquivo.getHash())) {
                throw new IllegalStateException("Falha de integridade do arquivo");
            }
            return new ByteArrayResource(Objects.requireNonNull(bytes, "Conteudo indisponivel"));
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possivel recuperar o arquivo PDF", e);
            }
        }
    
    /** Pre validacao do UPLOAD */
    public void validar(MultipartFile file) { 
        obterEValidarConteudo(file);
    }

    private Arquivo salvar(MultipartFile file, Ata ata, Usuario usuario) {
        byte[] original = obterEValidarConteudo(file);
        String nomeArmazenado = storage.gerarNomeUnico();

        try {
            storage.armazenarCriptografado(original, nomeArmazenado);

            Arquivo arquivo = new Arquivo();
            arquivo.setNomeOriginal(file.getOriginalFilename());
            arquivo.setNomeArmazenado(nomeArmazenado);
            arquivo.setTipoConteudo(TIPO_PDF);
            arquivo.setTamanho((long) original.length);
            arquivo.setCriptografado(true);
            arquivo.setHash(pdf.calcularHash(original));
            arquivo.setDataEnvio(LocalDateTime.now());
            arquivo.setAta(ata);
            arquivo.setUsuario(usuario);

            Arquivo salvo = arquivos.save(arquivo);
            ata.setArquivo(salvo);
            atas.save(ata);
            return salvo;
        } catch (Exception e) {
            try {
                storage.excluir(nomeArmazenado);
            } catch (IOException ignored) {

            }
            throw new IllegalStateException("Não foi possivel armazenar o PDF", e);
        }
    }

    private byte[] obterEValidarConteudo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo Vazio");
        }
        if (file.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Tamanho maximo excedido, favor verificar. Limite de 10 MB");
        }

        String nomeOriginal = file.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Envie um arquivo com extensão .pdf");
        }
        if (!TIPO_PDF.equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Tipo de arquivo invalido");
        }

        try {
            byte[] conteudo = Objects.requireNonNull(
                file.getBytes(),"Nao foi possivel ler o conteudo do arquivo");
            if (conteudo.length == 0) {
                throw new IllegalArgumentException("Arquivo Vazio");
            }
            pdf.validarPdf(conteudo);
            return conteudo;       
        } catch (IOException e) {
            throw new SecurityException("Permissão Insuficiente para Upload de arquivos");
        }
    }

    private void validarPermissaoUpload(Usuario usuario) {
        Usuario usuarioObrigatorio = Objects.requireNonNull(usuario, "Usuario é obrigatorio");

        if (usuarioObrigatorio.getPerfil() != Usuario.Perfil.COLABORADOR && usuarioObrigatorio.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Sem permissao para Upload");
        }
    }
}