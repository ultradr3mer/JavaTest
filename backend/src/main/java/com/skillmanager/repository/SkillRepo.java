package com.skillmanager.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

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
            if (Paths.get(entryName).getFileName().toString().equalsIgnoreCase("skill.md")) {
                skillMdExists = true;
            }

            Path entryPath = entry.getName().startsWith(skillName) ? baseDir : skillDir;
            entryPath = entryPath.resolve(entry.getName());

            if (!entryPath.startsWith(skillDir)) {
                throw new IOException("ZIP-Eintrag verlässt das Zielverzeichnis: " + entry.getName());
            }

            Path zipEntryDir = entryPath.getParent();
            Files.createDirectories(zipEntryDir);

            Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);

            zis.closeEntry();
        }

        if (!skillMdExists) {
            FileUtils.deleteDirectory(skillDir.toFile());
            throw new InvalidSkillException("ZIP-Datei enthält keine gültige skill.md-Datei.");
        }
    }
}
