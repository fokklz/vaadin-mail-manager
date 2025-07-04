package ch.aarboard.vamm.ldap;

import ch.aarboard.vamm.config.LdapConfig;
import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.data.entries.JammPostmaster;
import ch.aarboard.vamm.security.SecurityService;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.Map;

@Service
public class LdapSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(LdapSessionManager.class);

    private final LdapConfig ldapConfig;
    private final SecurityService securityService;

    @Autowired
    public LdapSessionManager(LdapConfig ldapConfig, SecurityService securityService) {
        this.ldapConfig = ldapConfig;
        this.securityService = securityService;
    }


    /**
     * Creates a new LdapTemplate for user operations.
     * Uses fixed credentials in test environment.
     *
     * @return A configured LdapTemplate instance.
     */
    public LdapTemplate createUserLdapTemplate() {
        if (securityService.isTestMode() ||
                (securityService.getCurrentUserDn().isEmpty() &&
                        securityService.getCurrentUserPassword().isEmpty())) {
            return createLdapTemplate("cn=admin,dc=example,dc=com","admin");
        } else {
            String userDn = securityService.getCurrentUserDn()
                    .orElseThrow(() -> new IllegalStateException("Kein LDAP-Benutzer-DN verfügbar"));
            String password = securityService.getCurrentUserPassword()
                    .orElseThrow(() -> new IllegalStateException("Kein LDAP-Passwort verfügbar"));
            return createLdapTemplate(userDn, password);
        }
    }

    /**
     * Creates a new LdapTemplate with the provided user DN and password.
     * This is used for operations that require user-specific authentication.
     *
     * @param userDn The distinguished name of the user.
     * @param password The password of the user.
     * @return A configured LdapTemplate instance.
     */
    public LdapTemplate createLdapTemplate(String userDn, String password) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapConfig.getUrl());
        contextSource.setBase(ldapConfig.getBase());
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);

        contextSource.setBaseEnvironmentProperties(Map.of(
                "com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapConfig.getConnectionTimeout()),
                "com.sun.jndi.ldap.read.timeout", String.valueOf(ldapConfig.getReadTimeout())
        ));

        contextSource.afterPropertiesSet();

        LdapTemplate template = new LdapTemplate(contextSource);
        DefaultObjectDirectoryMapper odm = new DefaultObjectDirectoryMapper();

        odm.manageClass(JammMailAccount.class);
        odm.manageClass(JammMailAlias.class);
        odm.manageClass(JammPostmaster.class);
        odm.manageClass(JammVirtualDomain.class);

        template.setObjectDirectoryMapper(odm);

        var orgDn = LdapNameBuilder.newInstance().add("o", "hosting").build();
        if (!doesOrganizationExist(template, orgDn)){
            Attributes attributes = new BasicAttributes();

            // Add object classes
            BasicAttribute objectClass = new BasicAttribute("objectClass");
            objectClass.add("top");
            objectClass.add("organization");
            attributes.put(objectClass);

            // Add organization name
            attributes.put(new BasicAttribute("o", "hosting"));

            template.bind(orgDn, null, attributes);
        }

        return template;
    }


    public boolean doesOrganizationExist(LdapTemplate template, Name orgDn) {
        try {
            return template.lookup(orgDn) != null;
        } catch (Exception e) {
            logger.debug("Organization does not exist: {}", e.getMessage());
            return false;
        }
    }

}