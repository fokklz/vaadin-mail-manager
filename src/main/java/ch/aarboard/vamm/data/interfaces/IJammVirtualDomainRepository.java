package ch.aarboard.vamm.data.interfaces;

import ch.aarboard.vamm.data.entries.JammVirtualDomain;

import java.util.List;
import java.util.Optional;

public interface IJammVirtualDomainRepository extends IJammRepository<JammVirtualDomain> {
    // Basic CRUD operations
    List<JammVirtualDomain> findAll();
    Optional<JammVirtualDomain> findByName(String domainName);
    boolean existsByName(String domainName);
    void deleteByName(String domainName);

    // Query methods
    List<JammVirtualDomain> findByAccountActiveFalse();
    List<JammVirtualDomain> findByDeleteTrue();
    List<JammVirtualDomain> findByAccountActiveTrue();
}
