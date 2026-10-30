package com.vutphala.resumeenhancer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Extracts plain text from uploaded PDF, DOCX and TXT resumes. */
@Component
public class ResumeTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(ResumeTextExtractor.class);

    public String extract(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        log.debug("Extracting text from '{}' ({} bytes)", name, file.getSize());
        try (InputStream in = file.getInputStream()) {
            String text = null;
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
                    log.debug("Parsing PDF with {} page(s)", doc.getNumberOfPages());
                    text = new PDFTextStripper().getText(doc);
                }
            } else if (name.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(in);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    log.debug("Parsing DOCX");
                    text = extractor.getText();
                }
            } else if (name.endsWith(".txt")) {
                log.debug("Reading plain text file");
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (text != null) {
                log.info("Extracted {} characters from '{}'", text.length(), name);
                return text;
            }
        }
        log.warn("Unsupported file type: '{}'", name);
        throw new UnsupportedFileTypeException("Unsupported file type. Please upload a PDF, DOCX or TXT file.");
    }

    public static class UnsupportedFileTypeException extends RuntimeException {
        public UnsupportedFileTypeException(String message) {
            super(message);
        }
    }
}
