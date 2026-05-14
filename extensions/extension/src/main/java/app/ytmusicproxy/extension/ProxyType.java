package app.ytmusicproxy.extension;

public enum ProxyType {
    HTTP,
    SOCKS;

    public static ProxyType fromPatchOption(String value) {
        if ("SOCKS".equalsIgnoreCase(value)) {
            return SOCKS;
        }

        return HTTP;
    }
}
