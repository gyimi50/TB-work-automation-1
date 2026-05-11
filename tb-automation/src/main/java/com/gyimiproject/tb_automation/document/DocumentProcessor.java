package com.gyimiproject.tb_automation.document;

import com.gyimiproject.tb_automation.document.parser.DocxParser;
import com.gyimiproject.tb_automation.document.parser.ToDocxConverter;
import com.gyimiproject.tb_automation.document.parser.SignatureExtractor;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DocumentProcessor {

    private final DocumentTypeDetector detector;
    private final DocxParser docxParser;
    private final SignatureExtractor signatureExtractor;
    private final ToDocxConverter converter;

    public DocumentProcessor(DocumentTypeDetector detector,
                             DocxParser docxParser,
                             SignatureExtractor signatureExtractor,
                             ToDocxConverter converter) {
        this.detector = detector;
        this.docxParser = docxParser;
        this.signatureExtractor = signatureExtractor;
        this.converter = converter;
    }

    public String process(File file) throws Exception {
        DocumentTypeDetector.DocumentType type = detector.detect(file);

        return switch (type) {
            case DOCX -> docxParser.parse(file) + "\n" + signatureExtractor.extract(file);
            case DOC, ODT -> {
                File converted = converter.convert(file);
                yield docxParser.parse(converted) + "\n" + signatureExtractor.extract(converted);
            }
            case UNSUPPORTED -> throw new UnsupportedOperationException(
                    "Unsupported file type: " + file.getName()
            );
        };
    }
}