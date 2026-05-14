package app.ytmusicproxy.extension;

import android.util.Log;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import org.chromium.net.CronetEngine;

@SuppressWarnings("unused")
public final class YouTubeMusicProxyPatch {
    private static final String TAG = "YTMProxyPatch";

    private YouTubeMusicProxyPatch() {
    }

    public static void apply(
            String proxyHost,
            String proxyPort,
            String proxyUsername,
            String proxyPassword
    ) {
        try {
            ProxyInstaller.setTrace(new ProxyInstaller.Trace() {
                @Override
                public void onSelect(URI uri, Proxy proxy) {
                    Log.i(TAG, "ProxySelector.select " + describeUri(uri) + " -> " + proxy);
                }

                @Override
                public void onConnectFailed(URI uri, SocketAddress socketAddress, IOException exception) {
                    Log.w(TAG, "Proxy connect failed for " + describeUri(uri) + " via " + socketAddress, exception);
                }
            });

            boolean applied = ProxyInstaller.apply(new ProxySettings() {
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

            Log.i(
                    TAG,
                    "Proxy setup " + (applied ? "applied" : "skipped")
                            + " host=" + proxyHost
                            + " port=" + proxyPort
                            + " auth=" + hasProxyAuth(proxyUsername, proxyPassword)
                            + " " + ProxyInstaller.describeState()
            );
        } catch (Exception ex) {
            Log.w(TAG, "Proxy setup failed", ex);
        }
    }

    public static boolean disableQuic(boolean original) {
        return false;
    }

    public static boolean enableMediaProxyResolver(boolean original) {
        ProxyInstaller.Snapshot snapshot = ProxyInstaller.getSnapshot();
        if (snapshot == null) {
            return original;
        }

        Log.i(TAG, "Media proxy resolver enabled for " + snapshot.getHost() + ":" + snapshot.getPort());
        return true;
    }

    public static void applyCronetProxyOptions(Object builder) {
        ProxyInstaller.Snapshot snapshot = ProxyInstaller.getSnapshot();
        if (snapshot == null) {
            return;
        }

        if (!(builder instanceof CronetEngine.Builder)) {
            Log.i(TAG, "Cronet proxy options skipped for " + describeObject(builder));
            return;
        }

        try {
            ((CronetEngine.Builder) builder).setProxyOptions(CronetProxyOptionsFactory.create(snapshot));
            Log.i(
                    TAG,
                    "Cronet proxy options applied host=" + snapshot.getHost()
                            + " port=" + snapshot.getPort()
                            + " auth=" + snapshot.hasAuthentication()
            );
        } catch (Throwable throwable) {
            Log.w(TAG, "Cronet proxy options failed", throwable);
        }
    }

    private static boolean hasProxyAuth(String username, String password) {
        return !nullToEmpty(username).isEmpty() || !nullToEmpty(password).isEmpty();
    }

    private static String describeUri(URI uri) {
        if (uri == null) {
            return "null";
        }

        return nullToEmpty(uri.getScheme()) + "://" + nullToEmpty(uri.getHost()) + ":" + uri.getPort();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String describeObject(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }
}
