package ch.aarboard.vamm.data.entries;

import org.junit.jupiter.api.Test;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JammVirtualDomainTest {

    private static final String TEST_DOMAIN = "example.com";

    void time(){
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }


    @Test
    void defaultConstructorSetsDefaultValues() {
        JammVirtualDomain domain = new JammVirtualDomain();

        assertEquals("TRUE", domain.getAccountActive());
        assertEquals("FALSE", domain.getDelete());
        assertEquals("TRUE", domain.getEditAccounts());
        assertEquals("TRUE", domain.getEditPostmasters());
        assertNotNull(domain.getLastChange());
        assertEquals("virtual:", domain.getPostfixTransport());
    }

    @Test
    void constructorWithDomainNameSetsCorrectValues() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);

        assertEquals(TEST_DOMAIN, domain.getJvd());
        assertEquals("TRUE", domain.getAccountActive());
        assertEquals("FALSE", domain.getDelete());

        Name expectedDn = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", TEST_DOMAIN)
                .build();
        assertEquals(expectedDn, domain.getId());
    }

    @Test
    void markForDeletionSetsCorrectFlags() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        String originalLastChange = domain.getLastChange();
        time();

        domain.markForDeletion();

        assertTrue(domain.isMarkedForDeletion());
        assertFalse(domain.isActive());
        assertNotEquals(originalLastChange, domain.getLastChange());
    }

    @Test
    void activateSetsCorrectFlags() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        domain.markForDeletion();
        String originalLastChange = domain.getLastChange();
        time();

        domain.activate();

        assertFalse(domain.isMarkedForDeletion());
        assertTrue(domain.isActive());
        assertNotEquals(originalLastChange, domain.getLastChange());
    }

    @Test
    void deactivateSetsCorrectFlags() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        String originalLastChange = domain.getLastChange();
        time();

        domain.deactivate();

        assertFalse(domain.isActive());
        assertNotEquals(originalLastChange, domain.getLastChange());
    }

    @Test
    void updateLastChangeUpdatesTimestamp() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        String originalLastChange = domain.getLastChange();
        time();

        domain.updateLastChange();

        assertNotEquals(originalLastChange, domain.getLastChange());
    }

    @Test
    void getLastChangeAsLongReturnsCorrectValue() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        long now = Instant.now().getEpochSecond();
        domain.setLastChange(String.valueOf(now));

        assertEquals(now, domain.getLastChangeAsLong());
    }

    @Test
    void setLastChangeAsLongSetsCorrectValue() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);
        long timestamp = Instant.now().getEpochSecond();

        domain.setLastChangeAsLong(timestamp);

        assertEquals(String.valueOf(timestamp), domain.getLastChange());
    }

    @Test
    void booleanMethodsWorkCorrectly() {
        JammVirtualDomain domain = new JammVirtualDomain(TEST_DOMAIN);

        domain.setActive(false);
        assertFalse(domain.isActive());
        assertEquals("FALSE", domain.getAccountActive());

        domain.setActive(true);
        assertTrue(domain.isActive());
        assertEquals("TRUE", domain.getAccountActive());

        domain.setMarkedForDeletion(true);
        assertTrue(domain.isMarkedForDeletion());
        assertEquals("TRUE", domain.getDelete());

        domain.setMarkedForDeletion(false);
        assertFalse(domain.isMarkedForDeletion());
        assertEquals("FALSE", domain.getDelete());

        domain.setCanEditAccounts(false);
        assertFalse(domain.canEditAccounts());
        assertEquals("FALSE", domain.getEditAccounts());

        domain.setCanEditAccounts(true);
        assertTrue(domain.canEditAccounts());
        assertEquals("TRUE", domain.getEditAccounts());

        domain.setCanEditPostmasters(false);
        assertFalse(domain.canEditPostmasters());
        assertEquals("FALSE", domain.getEditPostmasters());

        domain.setCanEditPostmasters(true);
        assertTrue(domain.canEditPostmasters());
        assertEquals("TRUE", domain.getEditPostmasters());
    }
}