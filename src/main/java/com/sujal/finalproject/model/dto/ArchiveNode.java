package com.sujal.finalproject.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArchiveNode {

    private String name;
    private String path;
    private String type;
    private Long sizeBytes;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ArchiveNode> children;

    public ArchiveNode(String name, String path, String type, Long sizeBytes) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.sizeBytes = sizeBytes;
        if ("FOLDER".equals(type)) {
            this.children = new ArrayList<>();
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public List<ArchiveNode> getChildren() { return children; }
    public void setChildren(List<ArchiveNode> children) { this.children = children; }
}