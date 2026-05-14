package app.ytmusicproxy.extension;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public final class ProxyInstaller {
    private ProxyInstaller() {
    }

    public static boolean apply(ProxySettings settings) {
        if (settings == null) {
            return false;
        }

        String host = trim(settings.getHost());
        Integer port = parsePort(settings.getPort());
        if (host.isEmpty() || port == null) {
            return false;
        }

        ProxyType type = settings.getType() == null ? ProxyType.HTTP : settings.getType();
        Proxy proxy = new Proxy(toJavaProxyType(type), InetSocketAddress.createUnresolved(host, port));
        ProxySelector.setDefault(new FixedProxySelector(proxy));
        applySystemProperties(type, host, port);
        applyAuthenticator(settings.getUsername(), settings.getPassword());
        return true;
    }

    private static void applySystemProperties(ProxyType type, String host, int port) {
        clearProxyProperties();

        if (type == ProxyType.SOCKS) {
            System.setProperty("socksProxyHost", host);
            System.setProperty("socksProxyPort", Integer.toString(port));
        } else {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", Integer.toString(port));
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", Integer.toString(port));
        }
    }

    private static void clearProxyProperties() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
    }

    private static void applyAuthenticator(String username, String password) {
        String proxyUsername = nullToEmpty(username);
        String proxyPassword = nullToEmpty(password);
        if (proxyUsername.isEmpty() && proxyPassword.isEmpty()) {
            return;
        }

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() != RequestorType.PROXY) {
                    return null;
                }

                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
            }
        });
    }

    private static Proxy.Type toJavaProxyType(ProxyType type) {
        return type == ProxyType.SOCKS ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
    }

    private static Integer parsePort(String value) {
        try {
            int port = Integer.parseInt(trim(value));
            return port >= 1 && port <= 65535 ? port : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String trim(String value) {
        return nullToEmpty(value).trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class FixedProxySelector extends ProxySelector {
        private final List<Proxy> proxies;

        private FixedProxySelector(Proxy proxy) {
            proxies = Collections.singletonList(proxy);
        }

        @Override
        public List<Proxy> select(URI uri) {
            return proxies;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress socketAddress, IOException exception) {
        }
    }
}
