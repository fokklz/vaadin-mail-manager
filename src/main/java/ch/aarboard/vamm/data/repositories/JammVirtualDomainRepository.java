package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import ch.aarboard.vamm.data.interfaces.IJammVirtualDomainRepository;
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
public class JammVirtualDomainRepository implements IJammVirtualDomainRepository {

    private static final Logger log = LoggerFactory.getLogger(JammVirtualDomainRepository.class);

    private LdapSessionManager ldapSessionManager;

    public JammVirtualDomainRepository(@Autowired LdapSessionManager ldapSessionManager) {
        this.ldapSessionManager = ldapSessionManager;
        log.debug("{} initialized with LdapSessionManager: {}", getClass().getName(), ldapSessionManager);
    }

    @Override
    public List<JammVirtualDomain> findAll() {
        try {
            List<JammVirtualDomain> domains = ldapSessionManager.createUserLdapTemplate().findAll(JammVirtualDomain.class);
            log.debug("Found {} domains", domains.size());
            return domains;
        } catch (Exception e) {
            log.error("Error finding all domains: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<JammVirtualDomain> findByName(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            log.debug("Domain name is null or empty, cannot check existence.");
            return Optional.empty();
        }

        try {
            Name dn = LdapUtils.domainDN(domainName).build();
            JammVirtualDomain domain = ldapSessionManager.createUserLdapTemplate().findByDn(dn, JammVirtualDomain.class);
            return Optional.of(domain);
        } catch (Exception e) {
            log.error("Error checking existence of domain {}: {}", domainName, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByName(String domainName) {
        var opt = findByName(domainName);
        return opt.isPresent();
    }

    @Override
    public void deleteByName(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            log.debug("Domain name is null or empty, cannot delete.");
            return;
        }

        try {
            findByName(domainName).ifPresent(this::delete);
        } catch (Exception e) {
            log.error("Failed to delete domain {}: {}", domainName, e.getMessage());
            throw new RuntimeException("Failed to delete domain: " + domainName, e);
        }
    }

    @Override
    public List<JammVirtualDomain> findByAccountActiveFalse() {
        try {
            List<JammVirtualDomain> domains = ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is(JammVirtualDomain.class.getName())
                            .and("accountActive").is("FALSE"),
                    JammVirtualDomain.class
            );
            log.debug("Found {} inactive domains", domains.size());
            return domains;
        } catch (Exception e) {
            log.error("Error finding inactive domains: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammVirtualDomain> findByDeleteTrue() {
        try {
            List<JammVirtualDomain> domains = ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is(JammVirtualDomain.class.getName())
                            .and("delete").is("TRUE"),
                    JammVirtualDomain.class
            );
            log.debug("Found {} domains marked for deletion", domains.size());
            return domains;
        } catch (Exception e) {
            log.error("Error finding domains marked for deletion: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammVirtualDomain> findByAccountActiveTrue() {
        try {
            List<JammVirtualDomain> domains = ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is(JammVirtualDomain.class.getName())
                            .and("accountActive").is("TRUE"),
                    JammVirtualDomain.class
            );
            log.debug("Found {} active domains", domains.size());
            return domains;
        } catch (Exception e) {
            log.error("Error finding active domains: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public JammVirtualDomain save(JammVirtualDomain domain) {
        if (domain.getId() == null) {
            Name dn = LdapUtils.domainDN(domain.getJvd()).build();
            domain.setId(dn);
        }

        domain.updateLastChange();

        try {
            JammMailAccount existing = null;
            try {
                existing = ldapSessionManager.createUserLdapTemplate().findByDn(domain.getId(), JammMailAccount.class);
            } catch (Exception ignored) { }

            if (existing != null) {
                ldapSessionManager.createUserLdapTemplate().update(domain);
                log.debug("Successfully updated domain: {}", domain.getJvd());
            } else {
                ldapSessionManager.createUserLdapTemplate().create(domain);
                log.debug("Successfully created domain: {}", domain.getJvd());
            }

            return domain;
        } catch (Exception e) {
            log.debug("Error saving domain {}: {}", domain.getJvd(), e.getMessage());
            throw new RuntimeException("Failed to save domain: " + domain.getJvd(), e);
        }
    }

    @Override
    public void delete(JammVirtualDomain entity) {
        if (entity.getId() == null) {
            log.warn("Cannot delete domain: {}", entity.getId());
            return;
        }

        try {
            ldapSessionManager.createUserLdapTemplate().delete(entity);
            log.info("Successfully deleted domain: {}", entity.getId());
        } catch (Exception e) {
            log.error("Failed to delete domain: {}. Error: {}", entity.getId(), e.getMessage());
            throw new RuntimeException("Failed to delete domain: " + entity.getId(), e);
        }
    }
}
