package org.example.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FaceService {

    public String extractEmbedding(String imagePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Users\\user\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
                    "D:\\java\\WasteWiseTn\\src\\main\\resources\\python\\face_register.py",
                    imagePath
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            String lastLine = null;

            while ((line = reader.readLine()) != null) {
                System.out.println(line); // debug console
                lastLine = line;
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return null;
            }

            if (lastLine == null || lastLine.isBlank()) {
                return null;
            }

            if (lastLine.startsWith("ERROR")) {
                return null;
            }

            return lastLine;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}