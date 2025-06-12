package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JammMailAliasRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private JammMailAliasRepository mailAliasRepository;

    @Autowired
    private JammVirtualDomainRepository virtualDomainRepository;

    private static final String TEST_ALIAS = "test.alias@example.com";
    private static final String TEST_DESTINATION = "destination@example.com";
    private static final String UPDATED_DESTINATION = "updated.destination@example.com";

    @Test
    @Order(1)
    public void testCreateMailAlias() {
        if (!virtualDomainRepository.existsByName("example.com")) {
            virtualDomainRepository.save(new JammVirtualDomain("example.com"));
        }

        JammMailAlias mailAlias = new JammMailAlias(TEST_ALIAS);
        mailAlias.addDestination(TEST_DESTINATION);

        JammMailAlias savedAlias = mailAliasRepository.save(mailAlias);

        assertNotNull(savedAlias.getId());
        assertEquals(TEST_ALIAS, savedAlias.getMail());
        assertEquals(TEST_DESTINATION, savedAlias.getDestinations().get(0));
    }

    @Test
    @Order(2)
    public void testFindMailAlias() {
        JammMailAlias foundAlias = mailAliasRepository.findByEmail(TEST_ALIAS).orElse(null);

        assertNotNull(foundAlias);
        assertEquals(TEST_ALIAS, foundAlias.getMail());
        assertEquals(TEST_DESTINATION, foundAlias.getDestinations().get(0));

        List<JammMailAlias> allAliases = mailAliasRepository.findByDomain("example.com");
        assertFalse(allAliases.isEmpty());
        assertTrue(allAliases.stream().anyMatch(alias -> TEST_ALIAS.equals(alias.getMail())));
    }

    @Test
    @Order(3)
    public void testUpdateMailAlias() {
        JammMailAlias aliasToUpdate = mailAliasRepository.findByEmail(TEST_ALIAS).orElse(null);
        assertNotNull(aliasToUpdate);

        aliasToUpdate.setDestinations(List.of(UPDATED_DESTINATION));

        JammMailAlias updatedAlias = mailAliasRepository.save(aliasToUpdate);

        assertEquals(UPDATED_DESTINATION, updatedAlias.getDestinations().get(0));
        assertEquals(TEST_ALIAS, updatedAlias.getMail());
    }

    @Test
    @Order(4)
    public void testExistsByEmail() {
        boolean exists = mailAliasRepository.existsByEmail(TEST_ALIAS);
        assertTrue(exists);

        boolean notExists = mailAliasRepository.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
    }

    @Test
    @Order(5)
    public void testDeleteMailAlias() {
        JammMailAlias aliasToDelete = mailAliasRepository.findByEmail(TEST_ALIAS).orElse(null);
        assertNotNull(aliasToDelete);

        mailAliasRepository.delete(aliasToDelete);

        JammMailAlias deletedAlias = mailAliasRepository.findByEmail(TEST_ALIAS).orElse(null);
        assertNull(deletedAlias);

        if (virtualDomainRepository.existsByName("example.com")) {
            virtualDomainRepository.deleteByName("example.com");
        }
    }
}