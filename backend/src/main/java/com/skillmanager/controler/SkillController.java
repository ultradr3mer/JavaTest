package com.skillmanager.controler;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.skillmanager.repository.SkillRepo;

@RestController
@RequestMapping("/api/skill")
@CrossOrigin(origins = "http://localhost:5173")
public class SkillController {

    private final SkillRepo skillRepo;

    public SkillController(SkillRepo skillRepo) {
        this.skillRepo = skillRepo;
    }

    @GetMapping
    public String getSkills() {
        return "SkillController is working!";
    }
    
    @PostMapping("/upload")
    public String uploadZip(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null || !originalFilename.endsWith(".zip")) {
            return "Ungültige Datei. Bitte eine ZIP-Datei hochladen.";
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            skillRepo.StoreSkill(zis, originalFilename.replace(".zip", ""));
            return "ZIP erfolgreich verarbeitet";
        } catch (SkillRepo.InvalidSkillException e) {
            return "Ungültige ZIP-Datei: " + e.getMessage();
        } catch (IOException e) {
            return "Fehler beim Entpacken";
        }
    }
}
