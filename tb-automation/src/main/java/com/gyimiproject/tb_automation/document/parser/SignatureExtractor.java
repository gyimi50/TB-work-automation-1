package com.gyimiproject.tb_automation.document.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class SignatureExtractor {

    public String extract(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFTable> tables = document.getTables();
            if (tables.isEmpty()) return "";

            XWPFTable lastTable = tables.get(tables.size() - 1);
            StringBuilder result = new StringBuilder();

            for (XWPFTableCell cell : lastTable.getRow(0).getTableCells()) {
                List<String> paragraphs = cell.getParagraphs().stream()
                        .map(p -> p.getText().trim())
                        .filter(t -> !t.isEmpty())
                        .toList();

                if (paragraphs.size() >= 2) {
                    result.append(paragraphs.get(0))
                            .append(" ")
                            .append(paragraphs.get(1))
                            .append("\n");
                }
            }
            return result.toString();
        }
    }
}