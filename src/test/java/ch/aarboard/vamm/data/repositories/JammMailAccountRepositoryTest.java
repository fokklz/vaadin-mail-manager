package ch.aarboard.vamm.data.repositories;

import ch.aarboard.vamm.data.entries.JammMailAccount;
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
public class JammMailAccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private JammMailAccountRepository mailAccountRepository;

    @Autowired
    private JammVirtualDomainRepository virtualDomainRepository;

    private static final String TEST_EMAIL = "test.user@example.com";
    private static final String UPDATED_NAME = "Updated Test User";

    @Test
    @Order(1)
    public void testCreateMailAccount() {
        // Ensure the virtual domain exists before creating a mail account
        if (!virtualDomainRepository.existsByName("example.com")) {
            virtualDomainRepository.save(new JammVirtualDomain("example.com"));
        }

        JammMailAccount mailAccount = new JammMailAccount(TEST_EMAIL, "/var/mail/vhosts/example.com", "test.user");
        mailAccount.setCommonName("Test User");
        mailAccount.setActive(true);

        JammMailAccount savedAccount = mailAccountRepository.save(mailAccount);

        assertNotNull(savedAccount.getId());
        assertEquals(TEST_EMAIL, savedAccount.getMail());
    }

    @Test
    @Order(2)
    public void testFindMailAccount() {
        // Suchen des Mail-Accounts nach E-Mail
        Optional<JammMailAccount> foundAccount = mailAccountRepository.findByEmail(TEST_EMAIL);

        assertTrue(foundAccount.isPresent());
        assertEquals(TEST_EMAIL, foundAccount.get().getMail());

        // Alle Mail-Accounts finden
        List<JammMailAccount> allAccounts = mailAccountRepository.findAll();
        assertFalse(allAccounts.isEmpty());
        assertTrue(allAccounts.stream().anyMatch(acc -> TEST_EMAIL.equals(acc.getMail())));
    }

    @Test
    @Order(3)
    public void testUpdateMailAccount() {
        // Account aktualisieren
        Optional<JammMailAccount> accountToUpdate = mailAccountRepository.findByEmail(TEST_EMAIL);
        assertTrue(accountToUpdate.isPresent());

        JammMailAccount account = accountToUpdate.get();
        account.setCommonName(UPDATED_NAME);

        JammMailAccount updatedAccount = mailAccountRepository.save(account);

        assertEquals(UPDATED_NAME, updatedAccount.getCommonName());
        assertEquals(TEST_EMAIL, updatedAccount.getMail());
    }

    @Test
    @Order(4)
    public void testDeleteMailAccount() {
        // Account löschen
        Optional<JammMailAccount> accountToDelete = mailAccountRepository.findByEmail(TEST_EMAIL);
        assertTrue(accountToDelete.isPresent());

        mailAccountRepository.delete(accountToDelete.get());

        // Überprüfen, dass der Account gelöscht wurde
        Optional<JammMailAccount> deletedAccount = mailAccountRepository.findByEmail(TEST_EMAIL);
        assertFalse(deletedAccount.isPresent());

        if (virtualDomainRepository.existsByName("example.com")) {
            // Remove the virtual domain again
            virtualDomainRepository.deleteByName("example.com");
        }
    }
}
