/*
 * Copyright 2025 Google LLC
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

package com.example.maps3djava.mainactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps3djava.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SampleListAdapter extends RecyclerView.Adapter<SampleListAdapter.ViewHolder> {

    private static final Set<Integer> TWO_D_SAMPLE_TITLES =
            new HashSet<>(
                    Arrays.asList(
                            com.example.maps3dcommon.R.string.feature_title_store_locator,
                            com.example.maps3dcommon.R.string.feature_title_routes_api,
                            com.example.maps3dcommon.R.string.feature_title_roadmap_mode,
                            com.example.maps3dcommon.R.string.feature_title_cloud_styling,
                            com.example.maps3dcommon.R.string.feature_title_data_visualization));

    public interface OnItemClickListener {
        void onClick(Class<?> activityClass, @StringRes int titleResId);
    }

    private final List<Map.Entry<Integer, Class<?>>> items;
    private final OnItemClickListener listener;

    public SampleListAdapter(Map<Integer, Class<?>> sampleMap, OnItemClickListener listener) {
        this.items = new ArrayList<>(sampleMap.entrySet());
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.sample_item_title);
        }

        @android.annotation.SuppressLint("SetTextI18n")
        void bind(
                Map.Entry<Integer, Class<?>> entry,
                Context context,
                OnItemClickListener listener) {
            String baseTitle = context.getString(entry.getKey());
            if (TWO_D_SAMPLE_TITLES.contains(entry.getKey())) {
                title.setText(baseTitle + "  [2D / ROADMAP]");
            } else {
                title.setText(baseTitle);
            }
            boolean enabled = entry.getValue() != null;
            title.setEnabled(enabled);
            title.setAlpha(enabled ? 1f : 0.5f);
            title.setOnClickListener(v -> listener.onClick(entry.getValue(), entry.getKey()));
        }
    }

    @NonNull
    @Override
    public SampleListAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.sample_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleListAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), holder.itemView.getContext(), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
