package com.backend.acervo.dto;

import java.time.LocalDateTime;
import com.backend.acervo.Arquivo;

public record ArquivoRespostaDTO(
    Long id, String nomeOriginal, String tipoConteudo, Long tamanho, boolean criptografado, LocalDateTime dataEnvio, Long ataIdLong
) {
    public static ArquivoRespostaDTO de(Arquivo arquivo) {
        return new ArquivoRespostaDTO(arquivo.getId(), arquivo.getNomeOriginal(), arquivo.getTipoConteudo(), arquivo.getTamanho(), arquivo.isCriptografado(),
        arquivo.getDataEnvio(), arquivo.getAta().getId());
    }
}