package com.skillmanager.repository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.skillmanager.model.SkillMdHeaderParser;

@Repository
public class SkillRepo {

    @Value("${skill.path}")
    private String skillPath;

    public class InvalidSkillException extends Exception {
        public InvalidSkillException(String message) {
            super(message);
        }
    }

    public void StoreSkill(ZipInputStream zis, String skillName) throws IOException, InvalidSkillException {
        Path baseDir = Paths.get(skillPath).toAbsolutePath().normalize();
        Path skillDir = baseDir.resolve(skillName).normalize();

        Files.createDirectories(baseDir);

        boolean skillMdExists = false;

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }

            String entryName = entry.getName();
            if(!entryName.toLowerCase().endsWith(".md")) {
                continue;
            }

            String content = new String(zis.readAllBytes());
            if (Paths.get(entryName).getFileName().toString().equalsIgnoreCase("skill.md")) {
                try {
                    SkillMdHeaderParser.Parse(content);
                } catch (SkillMdHeaderParser.InvalidHeaderException e) {
                    throw new InvalidSkillException("Ungültige skill.md-Header: " + e.getMessage());
                }

                skillMdExists = true;
            }

            Path entryPath = entry.getName().startsWith(skillName) ? baseDir : skillDir;
            entryPath = entryPath.resolve(entry.getName());

            if (!entryPath.startsWith(skillDir)) {
                throw new IOException("ZIP-Eintrag verlässt das Zielverzeichnis: " + entry.getName());
            }

            Path zipEntryDir = entryPath.getParent();
            Files.createDirectories(zipEntryDir);

            Files.writeString(entryPath, 
                            content, 
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);

            zis.closeEntry();
        }

        if (!skillMdExists) {
            FileUtils.deleteDirectory(skillDir.toFile());
            throw new InvalidSkillException("ZIP-Datei enthält keine gültige skill.md-Datei.");
        }
    }

    public Map<String, Map<String, String>> getAllSkills() throws IOException, SkillMdHeaderParser.InvalidHeaderException {
        Path baseDir = Paths.get(skillPath).toAbsolutePath().normalize();
        Map<String, Map<String, String>> skills = new HashMap<>();

        if (!Files.isDirectory(baseDir)) {
            throw new IOException("Skill-Verzeichnis existiert nicht: " + baseDir.toString());
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, Files::isDirectory)) {
            for (Path skillDir : stream) {
                Path skillMd = skillDir.resolve("skill.md");
                if (!Files.isRegularFile(skillMd)) {
                    throw new IOException("Fehlende skill.md-Datei im Verzeichnis: " + skillDir.toString());
                }
                String content = Files.readString(skillMd);
                Map<String, String> headers = SkillMdHeaderParser.Parse(content);
                skills.put(skillDir.getFileName().toString(), headers);
            }
        }

        return skills;
    }
}
