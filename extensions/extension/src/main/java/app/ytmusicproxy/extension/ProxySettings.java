package app.ytmusicproxy.extension;

public interface ProxySettings {
    ProxyType getType();

    String getHost();

    String getPort();

    String getUsername();

    String getPassword();
}
