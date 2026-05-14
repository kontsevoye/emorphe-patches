package com.google.android.apps.youtube.music.ui.preference;

import android.content.Context;
import android.os.Bundle;

public abstract class CustomPreferenceFragmentCompat {
    public Context getContext() {
        return null;
    }

    public abstract void onCreatePreferences(Bundle savedInstanceState, String rootKey);

    public void setPreferencesFromResource(int preferencesResId, String rootKey) {
    }
}
