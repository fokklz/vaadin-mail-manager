package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.data.interfaces.IJammMailAliasRepository;
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
public class JammMailAliasRepository implements IJammMailAliasRepository {

    private static final Logger log = LoggerFactory.getLogger(JammMailAliasRepository.class);

    private LdapSessionManager ldapSessionManager;

    public JammMailAliasRepository(@Autowired LdapSessionManager ldapSessionManager) {
        this.ldapSessionManager = ldapSessionManager;
        log.debug("{} initialized with LdapSessionManager: {}", getClass().getName(), ldapSessionManager);
    }

    @Override
    public Optional<JammMailAlias> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.debug("Email is null or empty, cannot find alias.");
            return Optional.empty();
        }

        try {
            Name dn = LdapUtils.mailDN(email).build();
            JammMailAlias alias = ldapSessionManager.createUserLdapTemplate().findByDn(dn, JammMailAlias.class);

            return Optional.of(alias);
        } catch (Exception e) {
            log.error("Error finding alias by email {}: {}", email, e.getMessage());
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
            log.error("Error checking existence of alias by email {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.debug("Email is null or empty, cannot delete alias.");
            return;
        }

        try {
            findByEmail(email).ifPresent(this::delete);
        } catch (Exception e) {
            log.error("Error deleting alias by email {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void deleteAllByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot delete aliases.");
            return;
        }

        try {
            List<JammMailAlias> aliases = findByDomain(domain);
            for (JammMailAlias alias : aliases) {
                delete(alias);
            }
        } catch (Exception e) {
            log.error("Error deleting aliases by domain {}: {}", domain, e.getMessage());
        }
    }

    @Override
    public List<JammMailAlias> findByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find aliases.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAlias"),
                    JammMailAlias.class
            );
        } catch (Exception e) {
            log.error("Error finding aliases by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammMailAlias> findByDomainExcludingSystem(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find aliases.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAlias")
                            .and("systemAlias").is("FALSE"),
                    JammMailAlias.class
            );
        } catch (Exception e) {
            log.error("Error finding non-system aliases by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public int countByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot count aliases.");
            return 0;
        }

        try {
            List<JammMailAlias> aliases = findByDomain(domain);
            return aliases.size();
        } catch (Exception e) {
            log.error("Error counting aliases by domain {}: {}", domain, e.getMessage());
            return 0;
        }
    }

    @Override
    public int countByDomainExcludingSystem(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot count non-system aliases.");
            return 0;
        }

        try {
            List<JammMailAlias> aliases = findByDomainExcludingSystem(domain);
            return aliases.size();
        } catch (Exception e) {
            log.error("Error counting non-system aliases by domain {}: {}", domain, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<JammMailAlias> findSystemAliasesByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find system aliases.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAlias")
                            .and("systemAlias").is("TRUE"),
                    JammMailAlias.class
            );
        } catch (Exception e) {
            log.error("Error finding system aliases by domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<JammMailAlias> findCatchAllByDomain(String domain) {
        // Catch-all alias has mail format: @domain.com
        String catchAllEmail = "@" + domain;
        return findByEmail(catchAllEmail);
    }

    @Override
    public List<JammMailAlias> findByDomainAndEmailContaining(String domain, String searchTerm) {
        if (domain == null || domain.isEmpty() || searchTerm == null || searchTerm.isEmpty()) {
            log.debug("Domain or search term is null or empty, cannot find aliases.");
            return List.of();
        }

        try {
            Name baseDn = LdapUtils.domainDN(domain).build();

            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is("JammMailAlias")
                            .and("mail").like("*" + LdapUtils.escape(searchTerm) + "*"),
                    JammMailAlias.class
            );
        } catch (Exception e) {
            log.error("Error finding aliases by domain {} and search term {}: {}", domain, searchTerm, e.getMessage());
            return List.of();
        }
    }

    @Override
    public JammMailAlias save(JammMailAlias alias) {
        if (alias.getId() == null) {
            String domain = alias.getDomain();
            Name dn = LdapUtils.mailDN(domain, alias.getMail()).build();
            alias.setId(dn);
        }

        alias.updateLastChange();

        try {
            var aliasOpt = findByEmail(alias.getMail());

            if (aliasOpt.isPresent()) {
                ldapSessionManager.createUserLdapTemplate().update(alias);
                log.debug("Successfully updated alias: {}", alias.getMail());
            } else {
                ldapSessionManager.createUserLdapTemplate().create(alias);
                log.debug("Successfully created alias: {}", alias.getMail());
            }

            return alias;
        } catch (Exception e) {
            log.debug("Error saving alias {}: {}", alias.getMail(), e.getMessage());
            throw new RuntimeException("Failed to save alias: " + alias.getMail(), e);
        }
    }

    @Override
    public void delete(JammMailAlias entity) {
        if (entity.getId() == null) {
            log.warn("Cannot delete alias without ID: {}", entity.getMail());
            return;
        }

        try {
            ldapSessionManager.createUserLdapTemplate().delete(entity);
            log.debug("Successfully deleted alias: {}", entity.getMail());
        } catch (Exception e) {
            log.error("Error deleting alias {}: {}", entity.getMail(), e.getMessage());
            throw new RuntimeException("Failed to delete alias: " + entity.getMail(), e);
        }
    }
}
