package ch.aarboard.vamm.data.entries;

import org.junit.jupiter.api.Test;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JammPostmasterTest {

    private static final String TEST_DOMAIN = "example.com";
    private static final String DEFAULT_ROLE_OCCUPANT = "cn=admin,o=hosting,dc=example,dc=com";
    private static final List<String> TEST_DESTINATIONS = Arrays.asList("admin@example.com", "backup@example.com");

    void time(){
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }


    @Test
    void defaultConstructorSetsDefaultValues() {
        JammPostmaster postmaster = new JammPostmaster();

        assertEquals("TRUE", postmaster.getAccountActive());
        assertNotNull(postmaster.getLastChange());
        assertNotNull(postmaster.getMaildrop());
        assertTrue(postmaster.getMaildrop().isEmpty());
        assertNotNull(postmaster.getRoleOccupant());
        assertTrue(postmaster.getRoleOccupant().isEmpty());
        assertEquals("postmaster", postmaster.getCommonName());
    }

    @Test
    void constructorWithDomainSetsCorrectValues() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);

        assertEquals("postmaster@" + TEST_DOMAIN, postmaster.getMail());
        assertEquals(1, postmaster.getMaildrop().size());
        assertTrue(postmaster.getMaildrop().contains("postmaster"));
        assertEquals(1, postmaster.getRoleOccupant().size());
        assertTrue(postmaster.getRoleOccupant().contains(DEFAULT_ROLE_OCCUPANT));

        Name expectedDn = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", TEST_DOMAIN)
                .add("cn", "postmaster")
                .build();
        assertEquals(expectedDn, postmaster.getId());
    }

    @Test
    void constructorWithDomainAndDestinationsSetsCorrectValues() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN, TEST_DESTINATIONS);

        assertEquals("postmaster@" + TEST_DOMAIN, postmaster.getMail());
        assertEquals(TEST_DESTINATIONS.size(), postmaster.getMaildrop().size());
        assertTrue(postmaster.getMaildrop().containsAll(TEST_DESTINATIONS));
    }

    @Test
    void isActiveReturnsCorrectValue() {
        JammPostmaster postmaster = new JammPostmaster();

        assertTrue(postmaster.isActive());

        postmaster.setActive(false);
        assertFalse(postmaster.isActive());

        postmaster.setActive(true);
        assertTrue(postmaster.isActive());
    }

    @Test
    void getDomainReturnsDomainPart() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);

        assertEquals(TEST_DOMAIN, postmaster.getDomain());
    }

    @Test
    void addRoleOccupantAddsNewRoleOccupant() {
        JammPostmaster postmaster = new JammPostmaster();
        String originalLastChange = postmaster.getLastChange();
        time();

        postmaster.addRoleOccupant("cn=new,o=hosting,dc=example,dc=com");

        assertTrue(postmaster.getRoleOccupant().contains("cn=new,o=hosting,dc=example,dc=com"));
        assertNotEquals(originalLastChange, postmaster.getLastChange());

        postmaster.addRoleOccupant("cn=new,o=hosting,dc=example,dc=com");
        assertEquals(1, postmaster.getRoleOccupant().size());
    }

    @Test
    void removeRoleOccupantRemovesExistingRoleOccupant() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);
        postmaster.addRoleOccupant("cn=second,o=hosting,dc=example,dc=com");
        String originalLastChange = postmaster.getLastChange();
        time();

        boolean result = postmaster.removeRoleOccupant("cn=second,o=hosting,dc=example,dc=com");

        assertTrue(result);
        assertEquals(1, postmaster.getRoleOccupant().size());
        assertTrue(postmaster.getRoleOccupant().contains(DEFAULT_ROLE_OCCUPANT));
        assertNotEquals(originalLastChange, postmaster.getLastChange());
    }

    @Test
    void removeLastRoleOccupantFailsToRemove() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);

        boolean result = postmaster.removeRoleOccupant(DEFAULT_ROLE_OCCUPANT);

        assertFalse(result);
        assertEquals(1, postmaster.getRoleOccupant().size());
        assertTrue(postmaster.getRoleOccupant().contains(DEFAULT_ROLE_OCCUPANT));
    }

    @Test
    void isRoleOccupantChecksIfUserIsRoleOccupant() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);

        assertTrue(postmaster.isRoleOccupant(DEFAULT_ROLE_OCCUPANT));
        assertFalse(postmaster.isRoleOccupant("cn=nonexistent,o=hosting,dc=example,dc=com"));
    }

    @Test
    void getRoleOccupantsReturnsUnmodifiableList() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);

        List<String> roleOccupants = postmaster.getRoleOccupants();
        assertEquals(1, roleOccupants.size());

        assertThrows(UnsupportedOperationException.class, () -> roleOccupants.add("cn=new,o=hosting,dc=example,dc=com"));
    }

    @Test
    void addDestinationAddsNewDestination() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN);
        String originalLastChange = postmaster.getLastChange();
        time();

        postmaster.addDestination("new@example.com");

        assertEquals(2, postmaster.getMaildrop().size());
        assertTrue(postmaster.getMaildrop().contains("new@example.com"));
        assertNotEquals(originalLastChange, postmaster.getLastChange());
    }

    @Test
    void removeDestinationRemovesExistingDestination() {
        JammPostmaster postmaster = new JammPostmaster(TEST_DOMAIN, TEST_DESTINATIONS);
        String originalLastChange = postmaster.getLastChange();
        time();

        boolean result = postmaster.removeDestination("admin@example.com");

        assertTrue(result);
        assertEquals(1, postmaster.getMaildrop().size());
        assertFalse(postmaster.getMaildrop().contains("admin@example.com"));
        assertNotEquals(originalLastChange, postmaster.getLastChange());
    }

    @Test
    void setRoleOccupantEnsuresAtLeastOneRoleOccupant() {
        JammPostmaster postmaster = new JammPostmaster();

        postmaster.setRoleOccupant(null);
        assertNotNull(postmaster.getRoleOccupant());
        assertEquals(1, postmaster.getRoleOccupant().size());

        postmaster.setRoleOccupant(List.of());
        assertNotNull(postmaster.getRoleOccupant());
        assertEquals(1, postmaster.getRoleOccupant().size());

        List<String> newOccupants = List.of("cn=user1,o=hosting", "cn=user2,o=hosting");
        postmaster.setRoleOccupant(newOccupants);
        assertEquals(2, postmaster.getRoleOccupant().size());
        assertTrue(postmaster.getRoleOccupant().containsAll(newOccupants));
    }

    @Test
    void updateLastChangeUpdatesTimestamp() {
        JammPostmaster postmaster = new JammPostmaster();
        String originalLastChange = postmaster.getLastChange();
        time();

        postmaster.updateLastChange();

        assertNotEquals(originalLastChange, postmaster.getLastChange());
    }
}