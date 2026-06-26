package com.skillmanager.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.skillmanager.data.SkillGetData;
import com.skillmanager.model.ParseResult;
import com.skillmanager.model.SkillMdHeaderParser;
import java.util.stream.Stream;

@Repository
public class SkillRepo {

    private static final String SKILL_MD = "skill.md";
    @Value("${skill.path}")
    private String skillPath;

    @Value("${skill.archive-path}")
    private String archivePath;

    public class InvalidSkillException extends Exception {
        public InvalidSkillException(String message) {
            super(message);
        }
    }

    public void storeSkill(ZipInputStream zis, String skillName) throws IOException, InvalidSkillException {
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
            if (!entryName.toLowerCase().endsWith(".md")) {
                continue;
            }

            String content = new String(zis.readAllBytes());
            if (Paths.get(entryName).getFileName().toString().equalsIgnoreCase(SKILL_MD)) {
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

    public Map<String, Map<String, String>> getAllSkills()
            throws IOException, SkillMdHeaderParser.InvalidHeaderException {
        Path baseDir = Paths.get(skillPath).toAbsolutePath().normalize();
        Map<String, Map<String, String>> skills = new HashMap<>();

        if (!Files.isDirectory(baseDir)) {
            throw new IOException("Skill-Verzeichnis existiert nicht: " + baseDir.toString());
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, Files::isDirectory)) {
            for (Path skillDir : stream) {
                Path skillMd = skillDir.resolve(SKILL_MD);
                if (!Files.isRegularFile(skillMd)) {
                    throw new IOException("Fehlende skill.md-Datei im Verzeichnis: " + skillDir.toString());
                }
                String content = Files.readString(skillMd);
                ParseResult parseResult = SkillMdHeaderParser.Parse(content);
                skills.put(skillDir.getFileName().toString(), parseResult.getHeaders());
            }
        }

        return skills;
    }

    public SkillGetData getSkill(String skillName, boolean stripHeader)
            throws IOException, SkillMdHeaderParser.InvalidHeaderException {
        Path skillDir = Paths.get(skillPath, skillName).toAbsolutePath().normalize();
        var files = new HashMap<String, String>();
        Map<String, String> header = null;
        try (Stream<Path> paths = Files.walk(skillDir)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                String content = Files.readString(path);

                if (path.getFileName().toString().equalsIgnoreCase(SKILL_MD)) {
                    ParseResult parseResult = SkillMdHeaderParser.Parse(content);
                    header = parseResult.getHeaders();
                    if (stripHeader) {
                        content = parseResult.getContentWithoutHeader();
                    }
                }

                files.put(skillDir.relativize(path).toString(), content);
            }
        }
        return new SkillGetData(skillName, header, files);
    }

    public byte[] getSkillAsZip(String skillName)
            throws IOException, SkillMdHeaderParser.InvalidHeaderException {
        SkillGetData skill = getSkill(skillName, false);
        return packSkillAsZip(skillName, skill.files);
    }

    public byte[] packSkillAsZip(String skillName, Map<String, String> files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String entryName = entry.getKey().replace('\\', '/');
                if (!entryName.startsWith(skillName + "/")) {
                    entryName = skillName + "/" + entryName;
                }
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue().getBytes());
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        }
    }

    public void archiveSkill(String skillName)
            throws IOException, SkillMdHeaderParser.InvalidHeaderException {
        Path skillDir = Paths.get(skillPath, skillName).toAbsolutePath().normalize();
        if (!Files.isDirectory(skillDir)) {
            throw new IOException("Skill-Verzeichnis existiert nicht: " + skillDir.toString());
        }

        SkillGetData skill = getSkill(skillName, false);
        byte[] zip = packSkillAsZip(skillName, skill.files);

        Path archiveDir = Paths.get(archivePath).toAbsolutePath().normalize();
        Files.createDirectories(archiveDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path archiveFile = archiveDir.resolve(skillName + "-" + timestamp + ".zip");
        Files.write(archiveFile, zip);

        FileUtils.deleteDirectory(skillDir.toFile());
    }
}
