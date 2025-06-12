package ch.aarboard.vamm.data.interfaces;

import ch.aarboard.vamm.data.entries.JammMailAccount;

import java.util.List;
import java.util.Optional;

public interface IJammMailAccountRepository extends IJammRepository<JammMailAccount> {
    // Basic CRUD operations
    List<JammMailAccount> findAll();
    Optional<JammMailAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
    void deleteAllByDomain(String domain);

    // Domain-based queries
    List<JammMailAccount> findByDomain(String domain);
    List<JammMailAccount> findByDomainAndEmailStartingWith(String domain, String prefix);
    List<JammMailAccount> findByDomainAndAccountActiveFalse(String domain);
    List<JammMailAccount> findByDomainAndDeleteTrue(String domain);

    // Count operations
    int countByDomain(String domain);

    // Search operations
    List<JammMailAccount> findByDomainAndEmailContaining(String domain, String searchTerm);
}
