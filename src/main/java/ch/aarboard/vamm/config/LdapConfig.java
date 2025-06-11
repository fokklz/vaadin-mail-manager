package ch.aarboard.vamm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapConfig {

    @Value("${jamm.ldap.url:ldap://localhost:389}")
    private String url;

    @Value("${jamm.ldap.base:dc=example,dc=com}")
    private String base;

    @Value("${jamm.ldap.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${jamm.ldap.read-timeout:10000}")
    private int readTimeout;

    public LdapConfig() {
        // Default constructor for Spring to create bean
    }

    public LdapConfig(String ldapUrl, String ldapBase, int connectionTimeout, int readTimeout) {
        this.url = ldapUrl;
        this.base = ldapBase;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public String getBase() {
        return base;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }
}