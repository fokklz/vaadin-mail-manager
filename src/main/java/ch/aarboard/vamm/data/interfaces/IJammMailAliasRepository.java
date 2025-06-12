package ch.aarboard.vamm.data.interfaces;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.entries.JammMailAlias;

import java.util.List;
import java.util.Optional;

public interface IJammMailAliasRepository extends IJammRepository<JammMailAlias> {
    // Basic CRUD operation
    Optional<JammMailAlias> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
    void deleteAllByDomain(String domain);

    // Domain-based queries
    List<JammMailAlias> findByDomain(String domain);
    List<JammMailAlias> findByDomainExcludingSystem(String domain);

    // Count operations
    int countByDomain(String domain);
    int countByDomainExcludingSystem(String domain);

    // System alias queries
    List<JammMailAlias> findSystemAliasesByDomain(String domain);
    Optional<JammMailAlias> findCatchAllByDomain(String domain);

    // Search operations
    List<JammMailAlias> findByDomainAndEmailContaining(String domain, String searchTerm);

}
