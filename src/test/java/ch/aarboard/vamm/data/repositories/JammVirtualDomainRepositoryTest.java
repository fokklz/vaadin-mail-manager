package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.base.AbstractRepositoryTest;
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
public class JammVirtualDomainRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private JammVirtualDomainRepository virtualDomainRepository;

    private static final String TEST_DOMAIN = "test-domain.com";
    private static final String SECOND_DOMAIN = "second-domain.com";

    @Test
    @Order(1)
    public void testCreateDomain() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);

        JammVirtualDomain savedDomain = virtualDomainRepository.save(domain);

        assertNotNull(savedDomain.getId());
        assertEquals(TEST_DOMAIN, savedDomain.getJvd());
        assertTrue(savedDomain.isActive());
        assertFalse(savedDomain.isMarkedForDeletion());
    }

    @Test
    @Order(2)
    public void testFindByName() {
        Optional<JammVirtualDomain> foundDomain = virtualDomainRepository.findByName(TEST_DOMAIN);

        assertTrue(foundDomain.isPresent());
        assertEquals(TEST_DOMAIN, foundDomain.get().getJvd());
    }

    @Test
    @Order(3)
    public void testExistsByName() {
        boolean exists = virtualDomainRepository.existsByName(TEST_DOMAIN);
        boolean notExists = virtualDomainRepository.existsByName("nonexistent-domain.com");

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @Order(4)
    public void testFindAll() {
        JammVirtualDomain secondDomain = new JammVirtualDomain(SECOND_DOMAIN);
        virtualDomainRepository.save(secondDomain);

        List<JammVirtualDomain> allDomains = virtualDomainRepository.findAll();

        assertFalse(allDomains.isEmpty());
        assertTrue(allDomains.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
        assertTrue(allDomains.stream().anyMatch(d -> SECOND_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(5)
    public void testDeactivateDomain() {
        Optional<JammVirtualDomain> domainToUpdate = virtualDomainRepository.findByName(TEST_DOMAIN);
        assertTrue(domainToUpdate.isPresent());

        JammVirtualDomain domain = domainToUpdate.get();
        domain.deactivate();

        JammVirtualDomain updatedDomain = virtualDomainRepository.save(domain);

        assertFalse(updatedDomain.isActive());
    }

    @Test
    @Order(6)
    public void testFindByAccountActiveFalse() {
        List<JammVirtualDomain> inactiveDomains = virtualDomainRepository.findByAccountActiveFalse();

        assertFalse(inactiveDomains.isEmpty());
        assertTrue(inactiveDomains.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(7)
    public void testActivateDomain() {
        Optional<JammVirtualDomain> domainToUpdate = virtualDomainRepository.findByName(TEST_DOMAIN);
        assertTrue(domainToUpdate.isPresent());

        JammVirtualDomain domain = domainToUpdate.get();
        domain.activate();

        JammVirtualDomain updatedDomain = virtualDomainRepository.save(domain);

        assertTrue(updatedDomain.isActive());
    }

    @Test
    @Order(8)
    public void testFindByAccountActiveTrue() {
        List<JammVirtualDomain> activeDomains = virtualDomainRepository.findByAccountActiveTrue();

        assertFalse(activeDomains.isEmpty());
        assertTrue(activeDomains.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(9)
    public void testMarkForDeletion() {
        Optional<JammVirtualDomain> domainToUpdate = virtualDomainRepository.findByName(TEST_DOMAIN);
        assertTrue(domainToUpdate.isPresent());

        JammVirtualDomain domain = domainToUpdate.get();
        domain.markForDeletion();

        JammVirtualDomain updatedDomain = virtualDomainRepository.save(domain);

        assertTrue(updatedDomain.isMarkedForDeletion());
    }

    @Test
    @Order(10)
    public void testFindByDeleteTrue() {
        List<JammVirtualDomain> domainsToDelete = virtualDomainRepository.findByDeleteTrue();

        assertFalse(domainsToDelete.isEmpty());
        assertTrue(domainsToDelete.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(11)
    public void testDeleteByName() {
        virtualDomainRepository.deleteByName(TEST_DOMAIN);

        Optional<JammVirtualDomain> deletedDomain = virtualDomainRepository.findByName(TEST_DOMAIN);
        assertFalse(deletedDomain.isPresent());
    }

    @Test
    @Order(12)
    public void testDeleteDomain() {
        Optional<JammVirtualDomain> domainToDelete = virtualDomainRepository.findByName(SECOND_DOMAIN);
        assertTrue(domainToDelete.isPresent());

        virtualDomainRepository.delete(domainToDelete.get());

        Optional<JammVirtualDomain> deletedDomain = virtualDomainRepository.findByName(SECOND_DOMAIN);
        assertFalse(deletedDomain.isPresent());
    }
}