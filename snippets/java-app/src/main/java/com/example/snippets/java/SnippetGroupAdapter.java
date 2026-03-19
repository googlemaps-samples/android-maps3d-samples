/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.snippets.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter supporting grouped category headers and snippet items representing the hierarchy.
 */
public class SnippetGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> items = new ArrayList<>();
    private final List<SnippetGroupInfo> groups;
    private final java.util.Set<String> expandedGroups = new java.util.HashSet<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SnippetItemInfo item);
    }

    public SnippetGroupAdapter(List<SnippetGroupInfo> groups, OnItemClickListener listener) {
        this.groups = groups;
        this.listener = listener;
        // All groups expanded by default
        for (SnippetGroupInfo group : groups) {
            expandedGroups.add(group.getTitle());
        }
        rebuildItems();
    }

    private void rebuildItems() {
        items.clear();
        for (SnippetGroupInfo group : groups) {
            items.add(group); // Header
            if (expandedGroups.contains(group.getTitle())) {
                items.addAll(group.getItems()); // Sub-items
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof SnippetGroupInfo) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_snippet, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            SnippetGroupInfo group = (SnippetGroupInfo) items.get(position);
            boolean isExpanded = expandedGroups.contains(group.getTitle());
            ((HeaderViewHolder) holder).bind(group, isExpanded, v -> {
                if (isExpanded) {
                    expandedGroups.remove(group.getTitle());
                } else {
                    expandedGroups.add(group.getTitle());
                }
                rebuildItems();
                notifyDataSetChanged();
            });
        } else {
            ((ItemViewHolder) holder).bind((SnippetItemInfo) items.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.headerTitle);
        }

        public void bind(SnippetGroupInfo group, boolean isExpanded, View.OnClickListener clickListener) {
            headerTitle.setText((isExpanded ? "▼ " : "▶ ") + group.getTitle());
            itemView.setOnClickListener(clickListener);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView description;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }

        public void bind(final SnippetItemInfo item, final OnItemClickListener listener) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
