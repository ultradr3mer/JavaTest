package com.skillmanager.controler;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import org.springframework.web.bind.annotation.CrossOrigin;
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
    
    @PostMapping("/upload")
    public String uploadZip(@RequestParam("file") MultipartFile file) {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            skillRepo.StoreSkill(zis);
            return "ZIP erfolgreich verarbeitet";

        } catch (IOException e) {
            return "Fehler beim Entpacken";
        }
    }
}
