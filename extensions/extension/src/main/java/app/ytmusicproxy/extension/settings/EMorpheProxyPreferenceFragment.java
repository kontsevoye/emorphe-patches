package app.ytmusicproxy.extension.settings;

import android.content.Context;
import android.os.Bundle;

import app.ytmusicproxy.extension.ProxySettingsStore;
import com.google.android.apps.youtube.music.ui.preference.CustomPreferenceFragmentCompat;

@SuppressWarnings("unused")
public final class EMorpheProxyPreferenceFragment extends CustomPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        getPreferenceManager().f(ProxySettingsStore.PREFERENCES_FILE);
        ProxySettingsStore.ensureDefaults(context);
        int resourceId = context.getResources().getIdentifier("emorphe_proxy_settings", "xml", context.getPackageName());
        if (resourceId != 0) {
            setPreferencesFromResource(resourceId, rootKey);
        }
    }
}
