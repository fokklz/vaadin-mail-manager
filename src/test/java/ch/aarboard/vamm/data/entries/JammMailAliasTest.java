package ch.aarboard.vamm.data.entries;

import org.junit.jupiter.api.Test;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JammMailAliasTest {

    private static final String TEST_MAIL = "alias@example.com";
    private static final String TEST_CATCH_ALL = "@example.com";
    private static final String TEST_COMMON_NAME = "Test Alias";
    private static final List<String> TEST_DESTINATIONS = Arrays.asList("user1@example.com", "user2@example.com");

    void time(){
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    @Test
    void defaultConstructorSetsDefaultValues() {
        JammMailAlias alias = new JammMailAlias();

        assertEquals("TRUE", alias.getAccountActive());
        assertNotNull(alias.getLastChange());
        assertNotNull(alias.getMaildrop());
        assertTrue(alias.getMaildrop().isEmpty());
    }

    @Test
    void constructorWithMailAndListDestinationsSetsCorrectValues() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL, TEST_DESTINATIONS, TEST_COMMON_NAME);

        assertEquals(TEST_MAIL, alias.getMail());
        assertEquals(TEST_DESTINATIONS.size(), alias.getMaildrop().size());
        assertEquals(TEST_COMMON_NAME, alias.getCommonName());
        assertTrue(alias.getMaildrop().containsAll(TEST_DESTINATIONS));

        Name expectedDn = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", "example.com")
                .add("mail", TEST_MAIL)
                .build();
        assertEquals(expectedDn, alias.getId());
    }

    @Test
    void constructorWithMailAndVarargDestinationsSetsCorrectValues() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL, "user1@example.com", "user2@example.com");

        assertEquals(TEST_MAIL, alias.getMail());
        assertEquals(2, alias.getMaildrop().size());
        assertTrue(alias.getMaildrop().contains("user1@example.com"));
        assertTrue(alias.getMaildrop().contains("user2@example.com"));

        Name expectedDn = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", "example.com")
                .add("mail", TEST_MAIL)
                .build();
        assertEquals(expectedDn, alias.getId());
    }

    @Test
    void isActiveReturnsCorrectValue() {
        JammMailAlias alias = new JammMailAlias();

        assertTrue(alias.isActive());

        alias.setActive(false);
        assertFalse(alias.isActive());

        alias.setActive(true);
        assertTrue(alias.isActive());
    }

    @Test
    void getAliasNameReturnsLocalPart() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL);

        assertEquals("alias", alias.getAliasName());
    }

    @Test
    void getDomainReturnsDomainPart() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL);

        assertEquals("example.com", alias.getDomain());
    }

    @Test
    void isCatchAllDetectsCatchAllAlias() {
        JammMailAlias regularAlias = new JammMailAlias(TEST_MAIL);
        JammMailAlias catchAllAlias = new JammMailAlias(TEST_CATCH_ALL);

        assertFalse(regularAlias.isCatchAll());
        assertTrue(catchAllAlias.isCatchAll());
    }

    @Test
    void addDestinationAddsNewDestination() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL);
        String originalLastChange = alias.getLastChange();
        time();

        alias.addDestination("new@example.com");

        assertEquals(1, alias.getMaildrop().size());
        assertTrue(alias.getMaildrop().contains("new@example.com"));
        assertNotEquals(originalLastChange, alias.getLastChange());

        alias.addDestination("new@example.com");
        assertEquals(1, alias.getMaildrop().size());
    }

    @Test
    void removeDestinationRemovesExistingDestination() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL, TEST_DESTINATIONS, TEST_COMMON_NAME);
        String originalLastChange = alias.getLastChange();
        time();

        boolean result = alias.removeDestination("user1@example.com");

        assertTrue(result);
        assertEquals(1, alias.getMaildrop().size());
        assertFalse(alias.getMaildrop().contains("user1@example.com"));
        assertNotEquals(originalLastChange, alias.getLastChange());

        result = alias.removeDestination("nonexistent@example.com");
        assertFalse(result);
    }

    @Test
    void getDestinationsReturnsUnmodifiableList() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL, TEST_DESTINATIONS, TEST_COMMON_NAME);

        List<String> destinations = alias.getDestinations();
        assertEquals(TEST_DESTINATIONS.size(), destinations.size());

        assertThrows(UnsupportedOperationException.class, () -> destinations.add("new@example.com"));
    }

    @Test
    void setDestinationsReplacesAllDestinations() {
        JammMailAlias alias = new JammMailAlias(TEST_MAIL, "old@example.com");
        String originalLastChange = alias.getLastChange();
        time();

        alias.setDestinations(TEST_DESTINATIONS);

        assertEquals(TEST_DESTINATIONS.size(), alias.getMaildrop().size());
        assertTrue(alias.getMaildrop().containsAll(TEST_DESTINATIONS));
        assertFalse(alias.getMaildrop().contains("old@example.com"));
        assertNotEquals(originalLastChange, alias.getLastChange());
    }

    @Test
    void updateLastChangeUpdatesTimestamp() {
        JammMailAlias alias = new JammMailAlias();
        String originalLastChange = alias.getLastChange();
        time();

        alias.updateLastChange();

        assertNotEquals(originalLastChange, alias.getLastChange());
    }

    @Test
    void getLastChangeAsLongReturnsCorrectValue() {
        JammMailAlias alias = new JammMailAlias();
        long now = Instant.now().getEpochSecond();
        alias.setLastChange(String.valueOf(now));

        assertEquals(now, alias.getLastChangeAsLong());
    }

    @Test
    void setLastChangeAsLongSetsCorrectValue() {
        JammMailAlias alias = new JammMailAlias();
        long timestamp = Instant.now().getEpochSecond();

        alias.setLastChangeAsLong(timestamp);

        assertEquals(String.valueOf(timestamp), alias.getLastChange());
    }
}