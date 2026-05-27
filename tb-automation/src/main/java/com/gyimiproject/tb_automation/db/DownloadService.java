package com.gyimiproject.tb_automation.db;

import org.openqa.selenium.Cookie;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Service
public class DownloadService {

    public String downloadFile(String downloadUrl, Set<Cookie> cookies, String downloadDir) {
        String cookieHeader = cookies.stream()
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(java.util.stream.Collectors.joining("; "));

        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                .build();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(downloadUrl))
                .header("Cookie", cookieHeader)
                .GET()
                .build();

        try {
            File dir = new File(downloadDir).getCanonicalFile();

            java.net.http.HttpResponse<java.io.InputStream> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofInputStream());

            String fileName = response.headers()
                    .firstValue("Content-Disposition")
                    .map(cd -> cd.replaceAll(".*filename[^;=\n]*=(['\"]?)([^'\"\n]*)\\1", "$2").trim())
                    .orElse(downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1));

            Path targetPath = dir.toPath().resolve(fileName);
            Files.copy(response.body(), targetPath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("=== DOWNLOADED: " + fileName + " ===");
            return fileName;

        } catch (IOException | InterruptedException e) {
            System.out.println("=== DOWNLOAD ERROR: " + e.getMessage() + " ===");
            return null;
        }
    }
}