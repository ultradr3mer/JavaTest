package com.skillmanager.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SkillRepo {

    @Value("${skill.path}")
    private String skillPath;

    public void StoreSkill(ZipInputStream zis) throws IOException {
        Path baseDir = Paths.get(skillPath).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }

            Path entryPath = baseDir.resolve(entry.getName()).normalize();

            if (!entryPath.startsWith(baseDir)) {
                throw new IOException("ZIP-Eintrag verlässt das Zielverzeichnis: " + entry.getName());
            }

            Path zipEntryDir = entryPath.getParent();
            Files.createDirectories(zipEntryDir);

            try (InputStream in = zis) {
                Files.copy(in, entryPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
