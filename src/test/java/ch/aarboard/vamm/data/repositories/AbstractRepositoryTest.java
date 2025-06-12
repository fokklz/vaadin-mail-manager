package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.config.LdapConfig;
import ch.aarboard.vamm.ldap.LdapSessionManager;
import ch.aarboard.vamm.security.SecurityService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.test.context.ActiveProfiles;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractRepositoryTest {

    @Autowired
    protected LdapConfig ldapConfig;

    @Autowired
    protected SecurityService securityService;

    protected LdapSessionManager ldapSessionManager;

    @BeforeAll
    public void setUp() {
        ldapSessionManager = new LdapSessionManager(
                ldapConfig,
                securityService
        );

        String baseDn = ldapConfig.getBase();
        LdapTemplate ldapTemplate = ldapSessionManager.createUserLdapTemplate();
    }
}