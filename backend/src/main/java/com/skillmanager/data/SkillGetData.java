package com.skillmanager.data;

import java.util.Map;

public class SkillGetData {

    public String skillName;
    public Map<String, String> header;
    public Map<String, String> files;

    public SkillGetData(String skillName, Map<String, String> header, Map<String, String> files) {
        this.skillName = skillName;
        this.header = header;
        this.files = files;
    }
}
