package ch.aarboard.vamm.base;

import ch.aarboard.vamm.config.LdapConfig;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import ch.aarboard.vamm.data.repositories.JammMailAccountRepository;
import ch.aarboard.vamm.data.repositories.JammMailAliasRepository;
import ch.aarboard.vamm.data.repositories.JammPostmasterRepository;
import ch.aarboard.vamm.data.repositories.JammVirtualDomainRepository;
import ch.aarboard.vamm.ldap.LdapSessionManager;
import ch.aarboard.vamm.security.SecurityService;
import ch.aarboard.vamm.services.JammMailAccountManagementService;
import ch.aarboard.vamm.services.JammMailAliasManagemeentService;
import ch.aarboard.vamm.services.JammVirtualDomainManagementService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractServiceTest {

    @Autowired
    protected LdapConfig ldapConfig;

    @Autowired
    protected SecurityService securityService;

    protected LdapSessionManager ldapSessionManager;

    protected JammVirtualDomainRepository virtualDomainRepository;

    protected JammMailAccountRepository mailAccountRepository;

    protected JammMailAliasRepository mailAliasRepository;

    protected JammPostmasterRepository postmasterRepository;

    protected JammVirtualDomainManagementService virtualDomainManagementService;

    protected JammMailAccountManagementService mailAccountManagementService;

    protected JammMailAliasManagemeentService mailAliasManagementService;


    @BeforeAll
    public void setUp() {
        ldapSessionManager = new LdapSessionManager(
                ldapConfig,
                securityService
        );

        virtualDomainRepository = new JammVirtualDomainRepository(ldapSessionManager);
        mailAccountRepository = new JammMailAccountRepository(ldapSessionManager);
        mailAliasRepository = new JammMailAliasRepository(ldapSessionManager);
        postmasterRepository = new JammPostmasterRepository(ldapSessionManager);

        virtualDomainManagementService = new JammVirtualDomainManagementService(
                virtualDomainRepository,
                mailAccountRepository,
                mailAliasRepository,
                postmasterRepository
        );

        mailAccountManagementService = new JammMailAccountManagementService(
                mailAccountRepository,
                mailAliasRepository,
                virtualDomainRepository
        );

        mailAliasManagementService = new JammMailAliasManagemeentService(
                mailAliasRepository,
                mailAccountRepository,
                virtualDomainRepository
        );

    }
}