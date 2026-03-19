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

import com.example.snippets.java.snippets.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;
import android.content.Context;

public class SnippetRegistry {

    private static final java.util.List<Object> addedElements = new java.util.ArrayList<>();

    public static void clearTrackedItems() {
        for (Object item : addedElements) {
            try {
                if (item instanceof com.google.android.gms.maps3d.model.Marker) ((com.google.android.gms.maps3d.model.Marker) item).remove();
                else if (item instanceof com.google.android.gms.maps3d.model.Polyline) ((com.google.android.gms.maps3d.model.Polyline) item).remove();
                else if (item instanceof com.google.android.gms.maps3d.model.Polygon) ((com.google.android.gms.maps3d.model.Polygon) item).remove();
                else if (item instanceof com.google.android.gms.maps3d.model.Model) ((com.google.android.gms.maps3d.model.Model) item).remove();
                else if (item instanceof com.google.android.gms.maps3d.Popover) ((com.google.android.gms.maps3d.Popover) item).remove();
            } catch (Exception e) { /* ignore */ }
        }
        addedElements.clear();
    }

    private static final List<Class<?>> snippetClasses = Arrays.asList(
        MapInitSnippets.class,
        CameraControlSnippets.class,
        MarkerSnippets.class,
        PolygonSnippets.class,
        PolylineSnippets.class,
        ModelSnippets.class,
        PopoverSnippets.class,
        PlaceSnippets.class
    );

    /**
     * Scans annotated classes to build the hierarchical snippet model.
     */
    public static List<SnippetGroupInfo> getSnippetGroups() {
        List<SnippetGroupInfo> groups = new ArrayList<>();

        for (Class<?> clazz : snippetClasses) {
            SnippetGroup groupAnnotation = clazz.getAnnotation(SnippetGroup.class);
            if (groupAnnotation == null) continue;

            List<SnippetItemInfo> items = new ArrayList<>();

            for (Method method : clazz.getDeclaredMethods()) {
                SnippetItem itemAnnotation = method.getAnnotation(SnippetItem.class);
                if (itemAnnotation == null) continue;

                items.add(new SnippetItemInfo(
                    itemAnnotation.title(),
                    itemAnnotation.description(),
                    groupAnnotation.title(),
                    (context, map) -> {
                        try {
                            TrackedMap3D trackedMap = new TrackedMap3D(map, addedElements);
                            Object instance = createInstance(clazz, context, trackedMap);
                            if (method.getParameterCount() == 0) {
                                method.invoke(instance);
                            } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Context.class) {
                                method.invoke(instance, context);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                ));
            }

            if (!items.isEmpty()) {
                items.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
                groups.add(new SnippetGroupInfo(
                    groupAnnotation.title(),
                    groupAnnotation.description(),
                    items
                ));
            }
        }
        return groups;
    }

    private static Object createInstance(Class<?> clazz, Context context, TrackedMap3D map) throws Exception {
        try {
            return clazz.getConstructor(Context.class, TrackedMap3D.class).newInstance(context, map);
        } catch (NoSuchMethodException e1) {
            try {
                return clazz.getConstructor(TrackedMap3D.class).newInstance(map);
            } catch (NoSuchMethodException e2) {
                return clazz.getConstructor().newInstance();
            }
        }
    }

    // Legacy support for Activities during refactoring
    public static final Map<String, SnippetItemInfo> snippets = new LinkedHashMap<>();

    static {
        for (SnippetGroupInfo group : getSnippetGroups()) {
            for (SnippetItemInfo item : group.getItems()) {
                snippets.put(group.getTitle() + " - " + item.getTitle(), item);
            }
        }
    }
}
