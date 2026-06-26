package com.skillmanager.controler;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.skillmanager.data.SkillGetData;
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
    public Map<String, Map<String, String>> getSkills() throws Exception {
        return skillRepo.getAllSkills();
    }

    @GetMapping("/{skillName}")
    public SkillGetData getSkills(@PathVariable String skillName) throws Exception {
        return skillRepo.getSkill(skillName, true);
    }

    @GetMapping("/{skillName}/download")
    public ResponseEntity<Resource> downloadSkill(@PathVariable String skillName) throws Exception {
        byte[] zip = skillRepo.getSkillAsZip(skillName);
        if (zip == null) {
            return ResponseEntity.notFound().build();
        }
        ByteArrayResource resource = new ByteArrayResource(zip);
        String filename = skillName + ".zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(zip.length)
                .body(resource);
    }

    @DeleteMapping("/{skillName}")
    public ResponseEntity<Void> archiveSkill(@PathVariable String skillName) {
        try {
            skillRepo.archiveSkill(skillName);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public String uploadZip(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".zip")) {
            return "Ungültige Datei. Bitte eine ZIP-Datei hochladen.";
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            skillRepo.storeSkill(zis, originalFilename.replace(".zip", ""));
            return "ZIP erfolgreich verarbeitet";
        } catch (SkillRepo.InvalidSkillException e) {
            return "Fehler beim Upload: " + e.getMessage();
        } catch (IOException e) {
            return "Fehler beim Entpacken";
        }
    }
}
