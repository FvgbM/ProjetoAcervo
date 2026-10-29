package com.backend.acervo.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ArquivoStorageService {

    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;
    private final Path diretorio;
    private final SecretKey chave;
    private final SecureRandom random = new SecureRandom();

    public ArquivoStorageService(
        @Value("${app.file.upload-dir}") String uploadDir,
        @Value("${app.file.encryption-key-base64}") String chaveBase64
    ) {
        this.diretorio = Path.of(uploadDir).toAbsolutePath().normalize();
         this.chave = criarChave(chaveBase64);

        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar diretório de arquivos", e);
        }
    }

    public String gerarNomeUnico() {
        return UUID.randomUUID() + ".bin";
    }

    public void armazenarCriptografado(byte[] conteudo, String nome) throws IOException {
        Path temporario = null;
        try {
            byte[] iv = new byte[TAMANHO_IV];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(conteudo);
            byte[] pacote = ByteBuffer.allocate(iv.length + cifrado.length)
                    .put(iv)
                    .put(cifrado)
                    .array();

            Path destino = resolver(nome);
            temporario = Files.createTempFile(diretorio, ".upload-", ".tmp");
            Files.write(temporario, pacote);

            try {
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Falha ao criptografar arquivo", e);
        } finally {
            if (temporario != null) {
                Files.deleteIfExists(temporario);
            }
        }
    }

    public byte[] recuperarDescriptografado(String nome) throws IOException {
        try {
            byte[] armazenado = Files.readAllBytes(resolver(nome));
            if (armazenado.length <= TAMANHO_IV) {
                throw new IOException("Arquivo criptografado inválido");
            }

            byte[] iv = java.util.Arrays.copyOfRange(armazenado, 0, TAMANHO_IV);
            byte[] cifrado = java.util.Arrays.copyOfRange(armazenado, TAMANHO_IV, armazenado.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            return cipher.doFinal(cifrado);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Falha ao descriptografar arquivo", e);
        }
    }

    public void excluir(String nome) throws IOException {
        Files.deleteIfExists(resolver(nome));
    }

    private SecretKey criarChave(String chaveBase64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(chaveBase64);
            if (bytes.length != 32) {
                throw new IllegalStateException("A chave de arquivos deve ter 32 bytes");
            }
            return new SecretKeySpec(bytes, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A chave de arquivos deve estar em Base64 válido", e);
        }
    }

    private Path resolver(String nome) {
        Path destino = diretorio.resolve(nome).normalize();
        if (!destino.startsWith(diretorio)) {
            throw new IllegalArgumentException("Nome de arquivo inválido");
        }
        return destino;
    }
}