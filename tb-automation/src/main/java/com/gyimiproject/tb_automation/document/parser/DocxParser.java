package com.gyimiproject.tb_automation.document.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class DocxParser {

    public String parse(File file) throws IOException {
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.getText(0);
                    if (text == null) continue;

                    if (run.isBold()) {
                        result.append("[b]").append(text).append("[/b]");
                    } else {
                        result.append(text);
                    }
                }
                result.append("\n");
            }
        }
        return result.toString();
    }
}