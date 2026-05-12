package com.gyimiproject.tb_automation.document.parser;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ToDocxConverter {

    private static final String SOFFICE_PATH =
            "C:/Program Files/LibreOffice/program/soffice.exe";

    public File convert(File inputFile) throws IOException, InterruptedException {
        File outputDir = new File(inputFile.getCanonicalFile().getParent(), "temp");
        outputDir.mkdirs();

        ProcessBuilder pb = new ProcessBuilder(
                SOFFICE_PATH,
                "--headless",
                "--convert-to", "docx",
                "--outdir", outputDir.getAbsolutePath(),
                inputFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("LibreOffice conversion failed with exit code: " + exitCode);
        }

        String originalName = inputFile.getName();
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        String docxFileName = baseName + ".docx";
        return new File(outputDir, docxFileName);
    }
}