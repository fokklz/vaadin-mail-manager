package ch.aarboard.vamm.services;

import ch.aarboard.vamm.base.AbstractServiceTest;
import ch.aarboard.vamm.data.entries.JammMailAlias;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JammMailAliasManagementServiceTest extends AbstractServiceTest {

    private static final String TEST_DOMAIN = "test-alias-domain.com";
    private static final String TEST_ALIAS = "test.alias@test-alias-domain.com";
    private static final String TEST_DESTINATION = "destination@example.com";
    private static final String SECOND_DESTINATION = "second@example.com";

    @Test
    @Order(1)
    public void testCreateDomainAndAlias() {
        virtualDomainManagementService.createDomain(TEST_DOMAIN, null);

        JammMailAlias alias = mailAliasManagementService.createAlias(
                TEST_ALIAS,
                List.of(TEST_DESTINATION),
                "Test Alias"
        );

        assertNotNull(alias);
        assertEquals(TEST_ALIAS, alias.getMail());
        assertEquals(1, alias.getDestinations().size());
        assertEquals(TEST_DESTINATION, alias.getDestinations().get(0));
    }

    @Test
    @Order(2)
    public void testGetAlias() {
        JammMailAlias alias = mailAliasManagementService.getAlias(TEST_ALIAS);

        assertNotNull(alias);
        assertEquals(TEST_ALIAS, alias.getMail());
        assertEquals(TEST_DESTINATION, alias.getDestinations().get(0));
    }

    @Test
    @Order(3)
    public void testAddDestination() {
        JammMailAlias alias = mailAliasManagementService.addDestination(TEST_ALIAS, SECOND_DESTINATION);

        assertEquals(2, alias.getDestinations().size());
        assertTrue(alias.getDestinations().contains(SECOND_DESTINATION));
    }

    @Test
    @Order(4)
    public void testRemoveDestination() {
        JammMailAlias alias = mailAliasManagementService.removeDestination(TEST_ALIAS, SECOND_DESTINATION);

        assertEquals(1, alias.getDestinations().size());
        assertFalse(alias.getDestinations().contains(SECOND_DESTINATION));
    }

    @Test
    @Order(5)
    public void testToggleAliasStatus() {
        JammMailAlias alias = mailAliasManagementService.toggleAliasStatus(TEST_ALIAS);

        assertFalse(alias.isActive());

        alias = mailAliasManagementService.toggleAliasStatus(TEST_ALIAS);

        assertTrue(alias.isActive());
    }

    @Test
    @Order(6)
    public void testGetAliasesByDomain() {
        List<JammMailAlias> aliases = mailAliasManagementService.getAliasesByDomain(TEST_DOMAIN);

        assertFalse(aliases.isEmpty());
        assertTrue(aliases.stream().anyMatch(a -> TEST_ALIAS.equals(a.getMail())));
    }

    @Test
    @Order(7)
    public void testCreateCatchAllAlias() {
        JammMailAlias catchAll = mailAliasManagementService.createCatchAllAlias(
                TEST_DOMAIN,
                List.of("catchall@example.com"),
                "Catch-All Alias"
        );

        assertNotNull(catchAll);
        assertEquals("@" + TEST_DOMAIN, catchAll.getMail());
        assertTrue(catchAll.isCatchAll());
    }

    @Test
    @Order(8)
    public void testDeleteAlias() {
        mailAliasManagementService.deleteAlias(TEST_ALIAS);

        assertThrows(IllegalArgumentException.class, () ->
                mailAliasManagementService.getAlias(TEST_ALIAS)
        );
    }

    @Test
    @Order(9)
    public void testCleanup() {
        mailAliasManagementService.deleteAllAliasesInDomain(TEST_DOMAIN);
        virtualDomainManagementService.deleteDomain(TEST_DOMAIN);

        assertFalse(virtualDomainManagementService.domainExists(TEST_DOMAIN));
    }
}