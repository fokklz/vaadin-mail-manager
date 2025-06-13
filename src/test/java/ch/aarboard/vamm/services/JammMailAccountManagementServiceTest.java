package ch.aarboard.vamm.services;

import ch.aarboard.vamm.base.AbstractServiceTest;
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
public class JammMailAccountManagementServiceTest extends AbstractServiceTest {

    private static final String TEST_DOMAIN = "test-service-domain.com";
    private static final String SECOND_DOMAIN = "second-service-domain.com";

    @Test
    @Order(1)
    public void testCreateDomain() {
        JammVirtualDomain domain = virtualDomainManagementService.createDomain(TEST_DOMAIN, null);

        assertNotNull(domain);
        assertEquals(TEST_DOMAIN, domain.getJvd());
        assertTrue(domain.isActive());
        assertFalse(domain.isMarkedForDeletion());
    }

    @Test
    @Order(2)
    public void testFindDomainByName() {
        JammVirtualDomain domain = virtualDomainManagementService.getDomain(TEST_DOMAIN);
        assertEquals(TEST_DOMAIN, domain.getJvd());
    }

    @Test
    @Order(3)
    public void testDomainExists() {
        boolean exists = virtualDomainManagementService.domainExists(TEST_DOMAIN);
        boolean notExists = virtualDomainManagementService.domainExists("nonexistent-domain.com");

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @Order(4)
    public void testGetAllDomains() {
        virtualDomainManagementService.createDomain(SECOND_DOMAIN, null);

        List<JammVirtualDomain> allDomains = virtualDomainManagementService.getAllDomains();

        assertFalse(allDomains.isEmpty());
        assertTrue(allDomains.stream().anyMatch(d -> TEST_DOMAIN.equals(d.getJvd())));
        assertTrue(allDomains.stream().anyMatch(d -> SECOND_DOMAIN.equals(d.getJvd())));
    }

    @Test
    @Order(5)
    public void testDeactivateDomain() {
        JammVirtualDomain deactivatedDomain = virtualDomainManagementService.deactivateDomain(TEST_DOMAIN);

        assertNotNull(deactivatedDomain);
        assertEquals(TEST_DOMAIN, deactivatedDomain.getJvd());
        assertFalse(deactivatedDomain.isActive());
    }

    @Test
    @Order(6)
    public void testActivateDomain() {
        JammVirtualDomain activatedDomain = virtualDomainManagementService.activateDomain(TEST_DOMAIN);

        assertNotNull(activatedDomain);
        assertEquals(TEST_DOMAIN, activatedDomain.getJvd());
        assertTrue(activatedDomain.isActive());
    }

    @Test
    @Order(7)
    public void testMarkDomainForDeletion() {
        JammVirtualDomain markedDomain = virtualDomainManagementService.markDomainForDeletion(TEST_DOMAIN);

        assertNotNull(markedDomain);
        assertEquals(TEST_DOMAIN, markedDomain.getJvd());
        assertTrue(markedDomain.isMarkedForDeletion());
    }

    @Test
    @Order(8)
    public void testDeleteDomain() {
        virtualDomainManagementService.deleteDomain(TEST_DOMAIN);
        assertFalse(virtualDomainManagementService.domainExists(TEST_DOMAIN));
    }
}