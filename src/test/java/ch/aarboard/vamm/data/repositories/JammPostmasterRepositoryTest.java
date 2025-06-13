package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.base.AbstractRepositoryTest;
import ch.aarboard.vamm.data.entries.JammPostmaster;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JammPostmasterRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private JammPostmasterRepository postmasterRepository;

    @Autowired
    private JammVirtualDomainRepository virtualDomainRepository;

    private static final String TEST_DOMAIN = "example.com";
    private static final String TEST_USER_DN = "cn=testuser,ou=users,dc=example,dc=com";
    private static final String UPDATED_USER_DN = "cn=updateduser,ou=users,dc=example,dc=com";

    @Test
    @Order(1)
    public void testCreatePostmaster() {
        if (!virtualDomainRepository.existsByName(TEST_DOMAIN)) {
            virtualDomainRepository.save(new JammVirtualDomain(TEST_DOMAIN));
        }

        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);
        postmaster.addRoleOccupant(TEST_USER_DN);

        JammPostmaster savedPostmaster = postmasterRepository.save(postmaster);

        assertNotNull(savedPostmaster.getId());
        assertEquals("postmaster@" + TEST_DOMAIN, savedPostmaster.getMail());
        assertTrue(savedPostmaster.getRoleOccupants().contains(TEST_USER_DN));
    }

    @Test
    @Order(2)
    public void testFindPostmaster() {
        Optional<JammPostmaster> foundPostmaster = postmasterRepository.findByDomain(TEST_DOMAIN);

        assertTrue(foundPostmaster.isPresent());
        assertEquals("postmaster@" + TEST_DOMAIN, foundPostmaster.get().getMail());
        assertTrue(foundPostmaster.get().getRoleOccupants().contains(TEST_USER_DN));
    }

    @Test
    @Order(3)
    public void testFindRoleOccupantsByDomain() {
        List<String> roleOccupants = postmasterRepository.findRoleOccupantsByDomain(TEST_DOMAIN);

        assertFalse(roleOccupants.isEmpty());
        assertTrue(roleOccupants.contains(TEST_USER_DN));
    }

    @Test
    @Order(4)
    public void testIsRoleOccupant() {
        boolean isRoleOccupant = postmasterRepository.isRoleOccupant(TEST_DOMAIN, TEST_USER_DN);
        assertTrue(isRoleOccupant);

        boolean isNotRoleOccupant = postmasterRepository.isRoleOccupant(TEST_DOMAIN, "cn=nonexistent,ou=users,dc=example,dc=com");
        assertFalse(isNotRoleOccupant);
    }

    @Test
    @Order(5)
    public void testFindDomainsByRoleOccupant() {
        List<String> domains = postmasterRepository.findDomainsByRoleOccupant(TEST_USER_DN);

        assertFalse(domains.isEmpty());
        assertTrue(domains.contains(TEST_DOMAIN));
    }

    @Test
    @Order(6)
    public void testFindPostmastersByRoleOccupant() {
        List<JammPostmaster> postmasters = postmasterRepository.findPostmastersByRoleOccupant(TEST_USER_DN);

        assertFalse(postmasters.isEmpty());
        assertTrue(postmasters.stream().anyMatch(pm -> TEST_DOMAIN.equals(pm.getDomain())));
    }

    @Test
    @Order(7)
    public void testUpdatePostmaster() {
        Optional<JammPostmaster> postmasterToUpdate = postmasterRepository.findByDomain(TEST_DOMAIN);
        assertTrue(postmasterToUpdate.isPresent());

        JammPostmaster postmaster = postmasterToUpdate.get();
        postmaster.setRoleOccupant(List.of(UPDATED_USER_DN));

        JammPostmaster updatedPostmaster = postmasterRepository.save(postmaster);

        assertEquals(1, updatedPostmaster.getRoleOccupants().size());
        assertTrue(updatedPostmaster.getRoleOccupants().contains(UPDATED_USER_DN));
        assertFalse(updatedPostmaster.getRoleOccupants().contains(TEST_USER_DN));
    }

    @Test
    @Order(8)
    public void testDeleteByDomain() {
        postmasterRepository.deleteByDomain(TEST_DOMAIN);

        Optional<JammPostmaster> postmaster = postmasterRepository.findByDomain(TEST_DOMAIN);
        assertTrue(postmaster.isPresent());
        // We always add at least one role occupant, so it should still exist
        assertTrue(postmaster.get().getRoleOccupants().size() == 1);
    }

    @Test
    @Order(9)
    public void testDeletePostmaster() {
        Optional<JammPostmaster> postmasterToDelete = postmasterRepository.findByDomain(TEST_DOMAIN);
        assertTrue(postmasterToDelete.isPresent());

        postmasterRepository.delete(postmasterToDelete.get());

        Optional<JammPostmaster> deletedPostmaster = postmasterRepository.findByDomain(TEST_DOMAIN);
        assertFalse(deletedPostmaster.isPresent());

        if (virtualDomainRepository.existsByName(TEST_DOMAIN)) {
            virtualDomainRepository.deleteByName(TEST_DOMAIN);
        }
    }
}