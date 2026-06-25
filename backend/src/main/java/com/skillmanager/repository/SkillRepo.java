package com.skillmanager.repository;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;

public class SkillRepo {

    @Value("${skill.path}")
    private String skillPath;

    public void StoreSkill(ZipInputStream zis) throws IOException {
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            System.out.println("Datei im ZIP: " + entry.getName());
        }

    }
}
