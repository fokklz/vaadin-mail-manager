package ch.aarboard.vamm.ldap;

import ch.aarboard.vamm.config.LdapConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;

@Configuration
public class LdapConfiguration {

    private final LdapConfig ldapConfig;

    @Autowired
    public LdapConfiguration(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapConfig.getUrl());
        contextSource.setBase(ldapConfig.getBase());

        contextSource.setAnonymousReadOnly(true);

        contextSource.setBaseEnvironmentProperties(java.util.Map.of(
                "com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapConfig.getConnectionTimeout()),
                "com.sun.jndi.ldap.read.timeout", String.valueOf(ldapConfig.getReadTimeout())
        ));

        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        LdapTemplate template = new LdapTemplate(ldapContextSource);
        DefaultObjectDirectoryMapper odm = new DefaultObjectDirectoryMapper();

        // TODO: add entries

        template.setObjectDirectoryMapper(odm);
        return template;
    }
}