package com.google.android.apps.youtube.music.ui.preference;

import android.content.Context;
import android.os.Bundle;

import defpackage.eni;

public abstract class CustomPreferenceFragmentCompat {
    public Context getContext() {
        throw new UnsupportedOperationException("Stub");
    }

    public eni getPreferenceManager() {
        throw new UnsupportedOperationException("Stub");
    }

    public abstract void onCreatePreferences(Bundle savedInstanceState, String rootKey);

    public void setPreferencesFromResource(int preferencesResId, String key) {
        throw new UnsupportedOperationException("Stub");
    }
}
