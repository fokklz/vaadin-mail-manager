package ch.aarboard.vamm.base;

import ch.aarboard.vamm.config.LdapConfig;
import ch.aarboard.vamm.ldap.LdapSessionManager;
import ch.aarboard.vamm.security.SecurityService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ActiveProfiles;

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