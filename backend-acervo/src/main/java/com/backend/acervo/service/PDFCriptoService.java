package com.backend.acervo.service;

import java.security.MessageDigest;
import java.util.HexFormat;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

@Service
public class PDFCriptoService {
    public void validarPdf(byte[] bytes) {
        try (PDDocument documento = Loader.loadPDF(bytes)) {
            if (documento.getNumberOfPages() < 1) throw new IllegalArgumentException("PDF sem páginas");
        } catch (Exception e) {
            throw new IllegalArgumentException("Arquivo PDF invalido ou corrompido", e);
        }
    }

    public String calcularHash(byte[] dados) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados)); }
        catch (Exception e) { throw new IllegalStateException("SHA-256 indisponivel", e); }
    }
}
