package ch.aarboard.vamm.services;

import ch.aarboard.vamm.base.AbstractServiceTest;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JammVirtualDomainManagementServiceTest extends AbstractServiceTest {

    private static final String TEST_DOMAIN = "test-vdomain-service.com";
    private static final String SECOND_DOMAIN = "second-vdomain-service.com";
    private static final String DOMAIN_DESCRIPTION = "Test Domain Description";

    @Test
    @Order(1)
    public void testCreateDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.createDomain(TEST_DOMAIN, DOMAIN_DESCRIPTION);

        assertNotNull(domain);
        assertEquals(TEST_DOMAIN, domain.getJvd());
        assertEquals(DOMAIN_DESCRIPTION, domain.getDescription());
        assertTrue(domain.isActive());
        assertFalse(domain.isMarkedForDeletion());
    }

    @Test
    @Order(2)
    public void testDomainExists() {
        boolean exists = virtualDomainManagementService.domainExists(TEST_DOMAIN);
        boolean notExists = virtualDomainManagementService.domainExists("nonexistent-domain.com");

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @Order(3)
    public void testGetDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.getDomain(TEST_DOMAIN);

        assertNotNull(domain);
        assertEquals(TEST_DOMAIN, domain.getJvd());
        assertEquals(DOMAIN_DESCRIPTION, domain.getDescription());
        assertTrue(domain.isActive());
    }

    @Test
    @Order(4)
    public void testGetDomainNoStats() {
        JammVirtualDomain domain = virtualDomainManagementService.getDomainNoStats(TEST_DOMAIN);

        assertNotNull(domain);
        assertEquals(TEST_DOMAIN, domain.getJvd());
        assertEquals(DOMAIN_DESCRIPTION, domain.getDescription());
    }

    @Test
    @Order(5)
    public void testCreateSecondDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.createDomain(SECOND_DOMAIN, null);

        assertNotNull(domain);
        assertEquals(SECOND_DOMAIN, domain.getJvd());
    }

    @Test
    @Order(6)
    public void testGetAllDomains() {
        List<JammVirtualDomain> domains = virtualDomainManagementService.getAllDomains();

        assertTrue(domains.size() >= 2);
        assertTrue(domains.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
        assertTrue(domains.stream().anyMatch(d -> SECOND_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(7)
    public void testGetAllDomainsWithStats() {
        List<JammVirtualDomain> domains = virtualDomainManagementService.getAllDomainsWithStats();

        assertTrue(domains.size() >= 2);
        domains.forEach(domain -> {
            assertNotNull(domain.getAccountCount());
            assertNotNull(domain.getAliasCount());
        });
    }

    @Test
    @Order(8)
    public void testGetDomainCount() {
        int count = virtualDomainManagementService.getDomainCount();

        assertTrue(count >= 2);
    }

    @Test
    @Order(9)
    public void testUpdateDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.getDomain(TEST_DOMAIN);
        String newDescription = "Updated Description";
        domain.setDescription(newDescription);

        JammVirtualDomain updatedDomain = virtualDomainManagementService.updateDomain(domain);

        assertEquals(newDescription, updatedDomain.getDescription());
    }

    @Test
    @Order(10)
    public void testToggleDomainStatus() {
        JammVirtualDomain domain = virtualDomainManagementService.toggleDomainStatus(TEST_DOMAIN);

        assertFalse(domain.isActive());

        domain = virtualDomainManagementService.toggleDomainStatus(TEST_DOMAIN);

        assertTrue(domain.isActive());
    }

    @Test
    @Order(11)
    public void testDeactivateDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.deactivateDomain(TEST_DOMAIN);

        assertFalse(domain.isActive());
    }

    @Test
    @Order(12)
    public void testActivateDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.activateDomain(TEST_DOMAIN);

        assertTrue(domain.isActive());
    }

    @Test
    @Order(13)
    public void testMarkDomainForDeletion() {
        JammVirtualDomain domain = virtualDomainManagementService.markDomainForDeletion(TEST_DOMAIN);

        assertTrue(domain.isMarkedForDeletion());
    }

    @Test
    @Order(14)
    public void testUnmarkDomainForDeletion() {
        JammVirtualDomain domain = virtualDomainManagementService.unmarkDomainForDeletion(TEST_DOMAIN);

        assertFalse(domain.isMarkedForDeletion());
    }

    @Test
    @Order(15)
    public void testDeleteSecondDomain() {
        virtualDomainManagementService.deleteDomain(SECOND_DOMAIN);

        assertFalse(virtualDomainManagementService.domainExists(SECOND_DOMAIN));
    }

    @Test
    @Order(16)
    public void testCleanup() {
        virtualDomainManagementService.deleteDomain(TEST_DOMAIN);

        assertFalse(virtualDomainManagementService.domainExists(TEST_DOMAIN));
    }
}