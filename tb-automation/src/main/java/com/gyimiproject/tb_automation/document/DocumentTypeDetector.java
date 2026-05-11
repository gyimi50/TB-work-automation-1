package com.gyimiproject.tb_automation.document;

import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class DocumentTypeDetector {

    public enum DocumentType {
        DOCX, ODT, DOC, UNSUPPORTED
    }

    public DocumentType detect(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".docx")) {
            return DocumentType.DOCX;
        } else if (fileName.endsWith(".odt")) {
            return DocumentType.ODT;
        } else if (fileName.endsWith(".doc")) {
            return DocumentType.DOC;
        }
        return DocumentType.UNSUPPORTED;
    }
}
