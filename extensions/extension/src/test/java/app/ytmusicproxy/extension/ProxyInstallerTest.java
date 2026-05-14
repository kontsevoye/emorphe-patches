package app.ytmusicproxy.extension;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

public class ProxyInstallerTest {
    public static void main(String[] args) throws Exception {
        testInvalidPortLeavesProxyStateUnchanged();
        testHttpProxySetsHttpAndHttpsProperties();
        testCredentialsInstallProxyAuthenticator();
        testSnapshotCapturesAppliedProxy();
        System.out.println("ProxyInstallerTest PASS");
    }

    private static void testInvalidPortLeavesProxyStateUnchanged() throws Exception {
        resetProxyState();
        ProxySelector originalSelector = ProxySelector.getDefault();

        ProxyInstaller.apply(settings("127.0.0.1", "70000", "", ""));

        assertSame(originalSelector, ProxySelector.getDefault(), "invalid port should not replace ProxySelector");
        assertNull(System.getProperty("https.proxyHost"), "invalid port should not set https proxy host");
    }

    private static void testHttpProxySetsHttpAndHttpsProperties() throws Exception {
        resetProxyState();

        ProxyInstaller.apply(settings("127.0.0.1", "8080", "", ""));

        assertEquals("127.0.0.1", System.getProperty("http.proxyHost"), "http host");
        assertEquals("8080", System.getProperty("http.proxyPort"), "http port");
        assertEquals("127.0.0.1", System.getProperty("https.proxyHost"), "https host");
        assertEquals("8080", System.getProperty("https.proxyPort"), "https port");

        List<Proxy> proxies = ProxySelector.getDefault().select(new URI("https://music.youtube.com/"));
        assertEquals(Proxy.Type.HTTP, proxies.get(0).type(), "proxy selector should return HTTP proxy");
    }

    private static void testCredentialsInstallProxyAuthenticator() {
        resetProxyState();

        ProxyInstaller.apply(settings("127.0.0.1", "8080", "alice", "secret"));

        PasswordAuthentication authentication = Authenticator.requestPasswordAuthentication(
                "127.0.0.1",
                null,
                8080,
                "http",
                "proxy auth",
                "Basic",
                null,
                Authenticator.RequestorType.PROXY
        );

        assertEquals("alice", authentication.getUserName(), "proxy username");
        assertEquals("secret", new String(authentication.getPassword()), "proxy password");
    }

    private static void testSnapshotCapturesAppliedProxy() {
        resetProxyState();

        ProxyInstaller.apply(settings("127.0.0.1", "8080", "alice", "secret"));

        ProxyInstaller.Snapshot snapshot = ProxyInstaller.getSnapshot();
        assertEquals("127.0.0.1", snapshot.getHost(), "snapshot host");
        assertEquals(8080, snapshot.getPort(), "snapshot port");
        assertEquals("alice", snapshot.getUsername(), "snapshot username");
        assertEquals("secret", snapshot.getPassword(), "snapshot password");
        assertEquals(true, snapshot.hasAuthentication(), "snapshot should capture auth");
    }

    private static ProxySettings settings(
            String host,
            String port,
            String username,
            String password
    ) {
        return new ProxySettings() {
            @Override
            public String getHost() {
                return host;
            }

            @Override
            public String getPort() {
                return port;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }
        };
    }

    private static void resetProxyState() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        Authenticator.setDefault(null);
    }

    private static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new AssertionError(message + ": expected null but was <" + actual + ">");
        }
    }
}
