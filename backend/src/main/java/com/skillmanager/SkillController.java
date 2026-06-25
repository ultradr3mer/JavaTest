package com.skillmanager;

import java.io.IOException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/skill")
@CrossOrigin(origins = "http://localhost:5173")
public class SkillController {

    @PostMapping("/upload")
    public String uploadZip(@RequestParam("file") MultipartFile file) {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Datei im ZIP: " + entry.getName());
                // Hier könntest du die Datei speichern
            }

            return "ZIP erfolgreich verarbeitet";

        } catch (IOException e) {
            return "Fehler beim Entpacken";
        }
    }
}
