package com.devtrack.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

/** Extracts plain text from an uploaded resume PDF using Apache PDFBox. */
public final class PdfTextExtractor {

    private PdfTextExtractor() {}

    public static String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new IOException("PDF is password-protected - please upload an unprotected file.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }
}
