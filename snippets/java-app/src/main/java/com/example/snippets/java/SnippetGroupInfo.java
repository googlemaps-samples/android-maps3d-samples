package com.example.snippets.java;

import java.util.List;

/**
 * Model class containing metadata and items for a grouped category of snippets.
 */
public class SnippetGroupInfo {
    private final String title;
    private final String description;
    private final List<SnippetItemInfo> items;

    public SnippetGroupInfo(String title, String description, List<SnippetItemInfo> items) {
        this.title = title;
        this.description = description;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<SnippetItemInfo> getItems() {
        return items;
    }
}
