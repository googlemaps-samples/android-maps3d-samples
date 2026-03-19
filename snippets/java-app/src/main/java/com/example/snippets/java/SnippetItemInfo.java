package com.example.snippets.java;

/**
 * Model class containing metadata and execution logic for an individual snippet item.
 */
public class SnippetItemInfo {
    private final String title;
    private final String description;
    private final String groupTitle;
    private final SnippetAction action;

    public SnippetItemInfo(String title, String description, String groupTitle, SnippetAction action) {
        this.title = title;
        this.description = description;
        this.groupTitle = groupTitle;
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public SnippetAction getAction() {
        return action;
    }
}
