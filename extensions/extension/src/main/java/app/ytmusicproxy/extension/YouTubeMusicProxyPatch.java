package app.ytmusicproxy.extension;

import android.util.Log;

@SuppressWarnings("unused")
public final class YouTubeMusicProxyPatch {
    private static final String TAG = "YTMProxyPatch";

    private YouTubeMusicProxyPatch() {
    }

    public static void apply(
            String proxyType,
            String proxyHost,
            String proxyPort,
            String proxyUsername,
            String proxyPassword
    ) {
        try {
            boolean applied = ProxyInstaller.apply(new ProxySettings() {
                @Override
                public ProxyType getType() {
                    return ProxyType.fromPatchOption(proxyType);
                }

                @Override
                public String getHost() {
                    return proxyHost;
                }

                @Override
                public String getPort() {
                    return proxyPort;
                }

                @Override
                public String getUsername() {
                    return proxyUsername;
                }

                @Override
                public String getPassword() {
                    return proxyPassword;
                }
            });

            Log.i(TAG, "Proxy setup " + (applied ? "applied" : "skipped"));
        } catch (Exception ex) {
            Log.w(TAG, "Proxy setup failed", ex);
        }
    }

    public static boolean disableQuic(boolean original) {
        return false;
    }
}
