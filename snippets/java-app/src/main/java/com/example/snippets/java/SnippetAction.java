package com.example.snippets.java;

import android.content.Context;
import com.google.android.gms.maps3d.GoogleMap3D;

public interface SnippetAction {
    void execute(Context context, GoogleMap3D map);
}
