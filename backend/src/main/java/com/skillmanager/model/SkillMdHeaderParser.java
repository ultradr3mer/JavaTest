package com.skillmanager.model;

import java.util.HashMap;
import java.util.Map;

public class SkillMdHeaderParser {

    public static class InvalidHeaderException extends Exception {
        public InvalidHeaderException(String message) {
            super(message);
        }
    }

    public static ParseResult Parse(String content) throws InvalidHeaderException {
        Map<String, String> headers = new HashMap<>();

        if (content == null || content.isBlank()) {
            throw new InvalidHeaderException("Skill.md ist leer.");
        }

        String trimmed = content.trim();
        if (!trimmed.startsWith("---")) {
            throw new InvalidHeaderException("Skill.md enthält keinen Frontmatter-Block (---).");
        }

        int start = trimmed.indexOf('\n') + 1;
        if (start == 0) {
            throw new InvalidHeaderException("Frontmatter-Block ist leer.");
        }

        int end = trimmed.indexOf("\n---", start);
        if (end == -1) {
            throw new InvalidHeaderException("Frontmatter-Block ist nicht geschlossen (--- fehlt).");
        }

        String headerString = trimmed.substring(start, end);

        for (String line : headerString.split("\n")) {
            int colon = line.indexOf(':');
            if (colon == -1) {
                continue;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            if (!key.isEmpty()) {
                headers.put(key, value);
            }
        }

        if (!headers.containsKey("name") || headers.get("name").isEmpty()) {
            throw new InvalidHeaderException("Frontmatter fehlt erforderliches Feld: name");
        }
        if (!headers.containsKey("description") || headers.get("description").isEmpty()) {
            throw new InvalidHeaderException("Frontmatter fehlt erforderliches Feld: description");
        }

        int contentStart = end + "\n---".length();
        String contentWithoutHeader = contentStart < trimmed.length()
                ? trimmed.substring(contentStart).stripLeading()
                : "";
                
        return new ParseResult(headers, contentWithoutHeader);
    }
}
