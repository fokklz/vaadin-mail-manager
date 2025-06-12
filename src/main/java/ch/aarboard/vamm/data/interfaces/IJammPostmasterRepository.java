package ch.aarboard.vamm.data.interfaces;

import ch.aarboard.vamm.data.entries.JammPostmaster;

import java.util.List;
import java.util.Optional;


public interface IJammPostmasterRepository extends IJammRepository<JammPostmaster> {
    // Basic CRUD operations
    Optional<JammPostmaster> findByDomain(String domain);
    void deleteByDomain(String domain);

    // Role occupant operations
    List<String> findRoleOccupantsByDomain(String domain);
    boolean isRoleOccupant(String domain, String userDn);

    List<String> findDomainsByRoleOccupant(String userDn);
    List<JammPostmaster> findPostmastersByRoleOccupant(String userDn);

}
