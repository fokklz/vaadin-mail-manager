package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammPostmaster;
import ch.aarboard.vamm.data.interfaces.IJammPostmasterRepository;
import ch.aarboard.vamm.ldap.LdapSessionManager;
import ch.aarboard.vamm.utils.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Repository;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JammPostmasterRepository implements IJammPostmasterRepository {

    private static final Logger log = LoggerFactory.getLogger(JammPostmasterRepository.class);

    private LdapSessionManager ldapSessionManager;

    public JammPostmasterRepository(@Autowired LdapSessionManager ldapSessionManager) {
        this.ldapSessionManager = ldapSessionManager;
        log.debug("{} initialized with LdapSessionManager: {}", getClass().getName(), ldapSessionManager);
    }

    @Override
    public Optional<JammPostmaster> findByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find postmaster.");
            return Optional.empty();
        }

        try {
            Name baseDn = LdapUtils.postmasterDN(domain).build();
            JammPostmaster postmaster = ldapSessionManager.createUserLdapTemplate().findOne(
                    LdapQueryBuilder.query()
                            .base(baseDn)
                            .where("objectClass").is(JammPostmaster.class.getName()),
                    JammPostmaster.class
            );

            return Optional.of(postmaster);
        } catch (Exception e) {
            log.error("Error finding accounts by domain {}: {}", domain, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void deleteByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot delete accounts.");
            return;
        }

        try {
            findByDomain(domain).ifPresent(postmaster -> {
                postmaster.setRoleOccupant(List.of());
                log.debug("Removed role occupants from postmaster for domain {}", domain);
            });
        } catch (Exception e) {
            log.error("Error deleting postmasters by domain {}: {}", domain, e.getMessage());
        }
    }

    @Override
    public List<String> findRoleOccupantsByDomain(String domain) {
        List<String> retVal = new ArrayList<>();

        if (domain == null || domain.isEmpty()) {
            log.debug("Domain is null or empty, cannot find role occupants.");
            return retVal;
        }

        findByDomain(domain).ifPresentOrElse(
                postmaster -> {
                    log.debug("Found postmaster for domain {}: {}", domain, postmaster.getMail());
                    retVal.addAll(postmaster.getRoleOccupants());
                },
                () -> {
                    log.debug("No postmaster found for domain {}", domain);
                }
        );
        return retVal;
    }

    @Override
    public boolean isRoleOccupant(String domain, String userDn) {
        if (domain == null || domain.isEmpty() || userDn == null || userDn.isEmpty()) {
            log.debug("Domain or userDn is null or empty, cannot check role occupant.");
            return false;
        }

        JammPostmaster postmaster = findByDomain(domain).orElse(null);
        if (postmaster == null) {
            log.debug("No postmaster found for domain {}", domain);
            return false;
        }

        return postmaster.getRoleOccupants().stream()
                .anyMatch(roleOccupant -> roleOccupant.equalsIgnoreCase(userDn));
    }

    @Override
    public List<String> findDomainsByRoleOccupant(String userDn) {
        if (userDn == null || userDn.isEmpty()) {
            log.debug("User DN is null or empty, cannot find domains by role occupant.");
            return List.of();
        }

        try {
            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is(JammPostmaster.class.getName())
                            .and("roleOccupants").like("*" + userDn + "*"),
                    JammPostmaster.class
            ).stream()
                    .map(JammPostmaster::getDomain)
                    .toList();
        } catch (Exception e) {
            log.error("Error finding domains by role occupant {}: {}", userDn, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<JammPostmaster> findPostmastersByRoleOccupant(String userDn) {
        if (userDn == null || userDn.isEmpty()) {
            log.debug("User DN is null or empty, cannot find postmasters by role occupant.");
            return List.of();
        }

        try {
            return ldapSessionManager.createUserLdapTemplate().find(
                    LdapQueryBuilder.query()
                            .where("objectClass").is(JammPostmaster.class.getName())
                            .and("roleOccupants").like("*" + userDn + "*"),
                    JammPostmaster.class
            );
        } catch (Exception e) {
            log.error("Error finding postmasters by role occupant {}: {}", userDn, e.getMessage());
            return List.of();
        }
    }

    @Override
    public JammPostmaster save(JammPostmaster postmaster) {
        if (postmaster.getId() == null) {
            String domain = postmaster.getDomain();
            Name dn = LdapUtils.postmasterDN(domain).build();
            postmaster.setId(dn);
        }

        postmaster.updateLastChange();

        try {
            var postmasterOpt = findByDomain(postmaster.getDomain());

            if (postmasterOpt.isPresent()) {
                ldapSessionManager.createUserLdapTemplate().update(postmaster);
                log.debug("Successfully updated postmaster: {}", postmaster.getMail());
            } else {
                ldapSessionManager.createUserLdapTemplate().create(postmaster);
                log.debug("Successfully created postmaster: {}", postmaster.getMail());
            }

            return postmaster;
        } catch (Exception e) {
            log.debug("Error saving postmaster {}: {}", postmaster.getMail(), e.getMessage());
            throw new RuntimeException("Failed to save postmaster: " + postmaster.getMail(), e);
        }
    }

    @Override
    public void delete(JammPostmaster entity) {
        if (entity.getId() == null) {
            log.warn("Cannot delete postmaster without ID: {}", entity.getDomain());
            return;
        }

        try {
            ldapSessionManager.createUserLdapTemplate().delete(entity);
            log.debug("Successfully deleted postmaster: {}", entity.getDomain());
        } catch (Exception e) {
            log.error("Error deleting postmaster {}: {}", entity.getDomain(), e.getMessage());
            throw new RuntimeException("Failed to delete postmaster: " + entity.getDomain(), e);
        }
    }
}
