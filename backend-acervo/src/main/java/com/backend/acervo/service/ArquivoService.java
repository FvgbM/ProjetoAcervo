package com.backend.acervo.service;

import com.backend.acervo.exceptions.ArquivoNaoEncontradoException;
import com.backend.acervo.Arquivo;
import com.backend.acervo.Usuario;
import com.backend.acervo.repository.ArquivoRepository;
import jakarta.transaction.Transactional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;
    private final Path rootLocation;

    public ArquivoService(ArquivoRepository arquivoRepository) {
        this.arquivoRepository = arquivoRepository;
        this.rootLocation = Path.of("uploads");
        init();
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar o diretório de uploads");
        }
    }

    @Transactional
    public Arquivo salvarArquivo(MultipartFile file, Usuario usuario) {
    if (file == null) {
        throw new IllegalArgumentException("O arquivo não pode ser nulo");
    }
    if (file.isEmpty()) {
        throw new IllegalArgumentException("O arquivo não pode estar vazio");
    }
    String originalFilename = StringUtils.cleanPath(
        Objects.requireNonNull(file.getOriginalFilename(), "Nome do arquivo não pode ser nulo")
    );
    if (!originalFilename.contains(".")) {
        throw new IllegalArgumentException("O arquivo deve ter uma extensão");
    } try {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID() + extension;

        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        Path destinationFile = rootLocation.resolve(newFilename)
            .normalize()
            .toAbsolutePath();

        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        

        Arquivo arquivo = new Arquivo();
        arquivo.setNomeOriginal(originalFilename);
        arquivo.setNomeArmazenado(newFilename);
        arquivo.setTipoConteudo(file.getContentType());
        arquivo.setTamanho(file.getSize());
        arquivo.setEnviadoPor(usuario);
        arquivo.setDataEnvio(LocalDateTime.now());
        return arquivoRepository.save(arquivo);
        
    } catch (IOException e) {
        throw new RuntimeException("Falha ao armazenar arquivo no sistema de arquivos", e);
    } catch (Exception e) {
        throw new RuntimeException("Erro inesperado ao processar o arquivo", e);
    }
}


    public Resource carregarArquivo(Long id) {
        Arquivo arquivo = arquivoRepository.findById(id)
            .orElseThrow(() -> new ArquivoNaoEncontradoException("Arquivo não encontrado com ID: " + id));
        
        try {
            Path file = rootLocation.resolve(arquivo.getNomeArmazenado());
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ArquivoNaoEncontradoException("Arquivo não encontrado no sistema de arquivos");
            }
        } catch (Exception e) {
            throw new ArquivoNaoEncontradoException("Erro ao carregar arquivo: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        Arquivo arquivo = arquivoRepository.findById(id)
            .orElseThrow(() -> new ArquivoNaoEncontradoException("Arquivo não encontrado com ID: " + id));
        
        try {
            Files.deleteIfExists(rootLocation.resolve(arquivo.getNomeArmazenado()));
            arquivoRepository.delete(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao deletar arquivo: " + e.getMessage());
        }
    }
}