package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.interfaces.IJammMailAccountRepository;
import ch.aarboard.vamm.ldap.LdapSessionManager;
import ch.aarboard.vamm.utils.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Repository;

import javax.naming.Name;
import java.util.List;
import java.util.Optional;

@Repository
public class JammMailAccountRepository implements IJammMailAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(JammMailAccountRepository.class);

    private LdapSessionManager ldapSessionManager;

    public JammMailAccountRepository(@Autowired LdapSessionManager ldapSessionManager) {
        this.ldapSessionManager = ldapSessionManager;
        log.debug("{} initialized with LdapSessionManager: {}", getClass().getName(), ldapSessionManager);
    }

    @Override
    public List<JammMailAccount> findAll(){
        try {
            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is("JammMailAccount"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding all mail accounts: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<JammMailAccount> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.debug("Email is null or empty, cannot find account.");
            return Optional.empty();
        }

        try {
            Name dn = LdapUtils.mailDN(email).build();
            JammMailAccount account =  ldapSessionManager.createUserLdapTemplate().findByDn(dn, JammMailAccount.class);

            return Optional.of(account);
        } catch (Exception e) {
            log.error("Error finding account by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.debug("Email is null or empty, cannot check existence.");
            return false;
        }

        try {
            return findByEmail(email).isPresent();
        } catch (Exception e) {
            log.error("Error checking existence of account by email {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.debug("Email is null or empty, cannot delete account.");
            return;
        }

        try {
            findByEmail(email).ifPresent(this::delete);
        } catch (Exception e) {
            log.error("Error deleting account by email {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to delete account: " + email, e);
        }

    }

    @Override
    public void deleteAllByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot delete accounts.");
            return;
        }

        try {
            List<JammMailAccount> accounts = findByDomain(domain);
            for (JammMailAccount account : accounts) {
                delete(account);
            }
        } catch (Exception e) {
            log.error("Error deleting accounts by domain {}: {}", domain, e.getMessage());
            throw new RuntimeException("Failed to delete accounts for domain: " + domain, e);
        }
    }

    @Override
    public List<JammMailAccount> findByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find accounts.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAccount"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding accounts by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammMailAccount> findByDomainAndEmailStartingWith(String domain, String prefix) {
        if (domain == null || domain.isEmpty() || prefix == null || prefix.isEmpty()) {
            log.debug("Domain or prefix is null or empty, cannot find accounts.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAccount")
                            .and("mail").like(prefix + "*"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding accounts by domain {} and prefix {}: {}", domain, prefix, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammMailAccount> findByDomainAndAccountActiveFalse(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find inactive accounts.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAccount")
                            .and("accountActive").is("FALSE"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding inactive accounts by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammMailAccount> findByDomainAndDeleteTrue(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find accounts marked for deletion.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAccount")
                            .and("delete").is("TRUE"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding accounts marked for deletion by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public int countByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot count accounts.");
            return 0;
        }

        try {
            List<JammMailAccount> accounts = findByDomain(domain);
            return accounts.size();
        } catch (Exception e) {
            log.error("Error counting accounts by domain {}: {}", domain, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<JammMailAccount> findByDomainAndEmailContaining(String domain, String searchTerm) {
        if (domain == null || domain.isEmpty() || searchTerm == null || searchTerm.isEmpty()) {
            log.debug("Domain or search term is null or empty, cannot find accounts.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAccount")
                            .and("mail").like("*" + LdapUtils.escape(searchTerm) + "*"),
                    JammMailAccount.class
            );
        } catch (Exception e) {
            log.error("Error finding accounts by domain {} and search term {}: {}", domain, searchTerm, e.getMessage());
            return List.of();
        }
    }

    @Override
    public JammMailAccount save(JammMailAccount account) throws RuntimeException {
        if (account.getId() == null) {
            String domain = account.getDomain();
            Name dn = LdapUtils.mailDN(domain, account.getMail()).build();
            account.setId(dn);
        }

        account.updateLastChange();

        try {
            var existingOpt = findByEmail(account.getMail());

            if (existingOpt.isPresent()) {
                ldapSessionManager.createUserLdapTemplate().update(account);
                log.debug("Successfully updated account: {}", account.getMail());
            } else {
                ldapSessionManager.createUserLdapTemplate().create(account);
                log.debug("Successfully created account: {}", account.getMail());
            }

            return account;
        } catch (Exception e) {
            log.debug("Error saving account {}: {}", account.getMail(), e.getMessage());
            throw new RuntimeException("Failed to save account: " + account.getMail(), e);
        }
    }

    @Override
    public void delete(JammMailAccount entity){
        if (entity.getId() == null) {
            log.warn("Cannot delete account without ID: {}", entity.getMail());
            return;
        }

        try {
            ldapSessionManager.createUserLdapTemplate().delete(entity);
            log.debug("Successfully deleted account: {}", entity.getMail());
        } catch (Exception e) {
            log.error("Error deleting account {}: {}", entity.getMail(), e.getMessage());
            throw new RuntimeException("Failed to delete account: " + entity.getMail(), e);
        }
    }
}
