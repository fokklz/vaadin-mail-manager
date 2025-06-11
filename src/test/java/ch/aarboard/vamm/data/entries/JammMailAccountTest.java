package ch.aarboard.vamm.data.entries;

import ch.aarboard.vamm.utils.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JammMailAccountTest {

    private static final String TEST_MAIL = "user@example.com";
    private static final String TEST_HOME_DIR = "/var/mail/vhosts/example.com";
    private static final String TEST_MAILBOX = "user";
    private static final String TEST_PASSWORD = "securePassword123";

    void time(){
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    @Test
    void defaultConstructorSetsDefaultValues() {
        JammMailAccount account = new JammMailAccount();

        assertEquals("TRUE", account.getAccountActive());
        assertEquals("FALSE", account.getDelete());
        assertNotNull(account.getLastChange());
    }

    @Test
    void constructorWithFieldsSetsCorrectValues() {
        JammMailAccount account = new JammMailAccount(TEST_MAIL, TEST_HOME_DIR, TEST_MAILBOX);

        assertEquals(TEST_MAIL, account.getMail());
        assertEquals(TEST_HOME_DIR, account.getHomeDirectory());
        assertEquals(TEST_MAILBOX, account.getMailbox());
        assertEquals("TRUE", account.getAccountActive());
        assertEquals("FALSE", account.getDelete());

        Name expectedDn = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", "example.com")
                .add("mail", TEST_MAIL)
                .build();
        assertEquals(expectedDn, account.getId());
    }

    @Test
    void setPasswordSecureHashesPassword() {
        JammMailAccount account = new JammMailAccount();
        String originalLastChange = account.getLastChange();
        time();

        account.setPasswordSecure(TEST_PASSWORD);

        assertEquals(TEST_PASSWORD, account.getClearPassword());
        assertNotNull(account.getUserPassword());
        assertTrue(account.getUserPassword().startsWith("{SSHA}"));
        assertNotEquals(originalLastChange, account.getLastChange());
        assertTrue(account.verifyPassword(TEST_PASSWORD));
    }

    @Test
    void setPasswordWithDifferentSchemes() {
        JammMailAccount account = new JammMailAccount();

        account.setPasswordWithScheme(TEST_PASSWORD, PasswordUtils.PasswordScheme.PLAIN);
        assertTrue(account.getUserPassword().startsWith("{PLAIN}"));
        assertTrue(account.verifyPassword(TEST_PASSWORD));

        account.setPasswordWithScheme(TEST_PASSWORD, PasswordUtils.PasswordScheme.SHA);
        assertTrue(account.getUserPassword().startsWith("{SHA}"));
        assertTrue(account.verifyPassword(TEST_PASSWORD));

        account.setPasswordWithScheme(TEST_PASSWORD, PasswordUtils.PasswordScheme.MD5);
        assertTrue(account.getUserPassword().startsWith("{MD5}"));
        assertTrue(account.verifyPassword(TEST_PASSWORD));

        account.setPasswordWithScheme(TEST_PASSWORD, PasswordUtils.PasswordScheme.SSHA);
        assertTrue(account.getUserPassword().startsWith("{SSHA}"));
        assertTrue(account.verifyPassword(TEST_PASSWORD));
    }

    @Test
    void verifyPasswordReturnsFalseForIncorrectPassword() {
        JammMailAccount account = new JammMailAccount();
        account.setPasswordSecure(TEST_PASSWORD);

        assertFalse(account.verifyPassword("wrongPassword"));
        assertTrue(account.verifyPassword(TEST_PASSWORD));
    }

    @Test
    void getFullPathToMailboxReturnsCorrectPath() {
        JammMailAccount account = new JammMailAccount(TEST_MAIL, TEST_HOME_DIR, TEST_MAILBOX);

        assertEquals(TEST_HOME_DIR + "/" + TEST_MAILBOX, account.getFullPathToMailbox());
    }

    @Test
    void getAccountNameReturnsLocalPart() {
        JammMailAccount account = new JammMailAccount(TEST_MAIL, TEST_HOME_DIR, TEST_MAILBOX);

        assertEquals("user", account.getAccountName());
    }

    @Test
    void getDomainReturnsDomainPart() {
        JammMailAccount account = new JammMailAccount(TEST_MAIL, TEST_HOME_DIR, TEST_MAILBOX);

        assertEquals("example.com", account.getDomain());
    }

    @Test
    void updateLastChangeUpdatesTimestamp() {
        JammMailAccount account = new JammMailAccount();
        String originalLastChange = account.getLastChange();
        time();

        account.updateLastChange();

        assertNotEquals(originalLastChange, account.getLastChange());
    }

    @Test
    void getLastChangeAsLongReturnsCorrectValue() {
        JammMailAccount account = new JammMailAccount();
        long now = Instant.now().getEpochSecond();
        account.setLastChange(String.valueOf(now));

        assertEquals(now, account.getLastChangeAsLong());
    }

    @Test
    void setLastChangeAsLongSetsCorrectValue() {
        JammMailAccount account = new JammMailAccount();
        long timestamp = Instant.now().getEpochSecond();

        account.setLastChangeAsLong(timestamp);

        assertEquals(String.valueOf(timestamp), account.getLastChange());
    }

    @Test
    void booleanMethodsWorkCorrectly() {
        JammMailAccount account = new JammMailAccount();

        account.setActive(false);
        assertFalse(account.isActive());
        assertEquals("FALSE", account.getAccountActive());

        account.setActive(true);
        assertTrue(account.isActive());
        assertEquals("TRUE", account.getAccountActive());

        account.setMarkedForDeletion(true);
        assertTrue(account.isMarkedForDeletion());
        assertEquals("TRUE", account.getDelete());

        account.setMarkedForDeletion(false);
        assertFalse(account.isMarkedForDeletion());
        assertEquals("FALSE", account.getDelete());
    }
}