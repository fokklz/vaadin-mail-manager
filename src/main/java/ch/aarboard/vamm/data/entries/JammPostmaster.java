package ch.aarboard.vamm.data.entries;

import ch.aarboard.vamm.utils.MailUtils;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * LDAP entity representing a Jamm postmaster.
 * Maps to the JammPostmaster auxiliary objectClass combined with JammMailAlias.
 * This represents a special alias that has postmaster privileges.
 */
@Entry(objectClasses = {"top", "JammMailAlias", "JammPostmaster"})
public final class JammPostmaster {

    @Id
    private Name id;

    // Inherited from JammMailAlias
    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "maildrop")
    private List<String> maildrop;

    // Boolean
    @Attribute(name = "accountActive")
    private String accountActive;

    // Long
    @Attribute(name = "lastChange")
    private String lastChange;

    @Attribute(name = "mailsource")
    private String mailsource;

    @Attribute(name = "cn")
    private String commonName;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "userPassword")
    private String userPassword;

    // JammPostmaster specific attribute - MUST have at least one value
    @Attribute(name = "roleOccupant")
    private List<String> roleOccupant;

    public JammPostmaster() {
        this.accountActive = "TRUE";
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
        this.maildrop = new ArrayList<>();
        this.roleOccupant = new ArrayList<>();
        this.commonName = "postmaster";
    }

    public JammPostmaster(String domain) {
        this();
        this.mail = "postmaster@" + domain;
        this.maildrop.add("postmaster"); // Default destination

        // Add a default role occupant - use the domain admin DN or a default DN
        // Since roleOccupant is MUST attribute, we need at least one value
        String defaultRoleOccupant = "cn=admin,o=hosting,dc=example,dc=com"; // Default admin DN
        this.roleOccupant.add(defaultRoleOccupant);

        // Auto-generate DN for postmaster
        this.id = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", domain)
                .add("cn", "postmaster")
                .build();
    }

    public JammPostmaster(String domain, List<String> destinations) {
        this(domain);
        this.maildrop = new ArrayList<>(destinations);
    }

    /**
     * Convenience method to check if the account is active.
     * @return true if the account is active, false otherwise.
     */
    public boolean isActive() {
        return "TRUE".equals(accountActive);
    }

    /**
     * Convenience method to set the account active status.
     * @param active true to activate the account, false to deactivate.
     */
    public void setActive(boolean active) {
        this.accountActive = active ? "TRUE" : "FALSE";
    }

    /**
     * Convenience method to get the domain from the postmaster email.
     * @return the domain part of the postmaster email.
     */
    public String getDomain() {
        return MailUtils.extractDomainFromMail(mail);
    }

    /**
     * Convenience method to add a role occupant (user DN).
     * This method ensures that the roleOccupant list is initialized and
     * does not contain duplicates.
     * @param userDn the distinguished name (DN) of the user to add as a role occupant.
     */
    public void addRoleOccupant(String userDn) {
        if (roleOccupant == null) {
            roleOccupant = new ArrayList<>();
        }
        if (!roleOccupant.contains(userDn)) {
            roleOccupant.add(userDn);
            updateLastChange();
        }
    }

    /**
     * Method to remove a role occupant (user DN).
     * This method ensures that at least one role occupant remains.
     * @param userDn the distinguished name (DN) of the user to remove as a role occupant.
     * @return true if the user was removed, false if it was the last occupant and could not be removed.
     */
    public boolean removeRoleOccupant(String userDn) {
        if (roleOccupant != null && roleOccupant.size() > 1) { // Keep at least one role occupant
            boolean removed = roleOccupant.remove(userDn);
            if (removed) {
                updateLastChange();
            }
            return removed;
        }
        return false; // Cannot remove the last role occupant
    }

    /**
     * Method to check if a user DN is a role occupant.
     * @param userDn the distinguished name (DN) of the user to check.
     * @return true if the user DN is a role occupant, false otherwise.
     */
    public boolean isRoleOccupant(String userDn) {
        return roleOccupant != null && roleOccupant.contains(userDn);
    }

    /**
     * Method to get a read-only view of the role occupants.
     * This returns an unmodifiable list of role occupant DNs.
     * @return an unmodifiable list of role occupant DNs.
     */
    public List<String> getRoleOccupants() {
        return roleOccupant != null ? Collections.unmodifiableList(roleOccupant) : Collections.emptyList();
    }

    /**
     * Method to add a destination to the maildrop.
     * This method ensures that the maildrop is initialized and does not contain duplicates.
     * @param destination the destination email address to add.
     */
    public void addDestination(String destination) {
        if (maildrop == null) {
            maildrop = new ArrayList<>();
        }
        if (!maildrop.contains(destination)) {
            maildrop.add(destination);
            updateLastChange();
        }
    }

    /**
     * Method to remove a destination from the maildrop.
     * @param destination the destination email address to remove.
     * @return true if the destination was removed, false otherwise.
     */
    public boolean removeDestination(String destination) {
        if (maildrop != null) {
            boolean removed = maildrop.remove(destination);
            if (removed) {
                updateLastChange();
            }
            return removed;
        }
        return false;
    }

    /**
     * Method to get a read-only view of the maildrop destinations.
     * @return an unmodifiable list of maildrop destinations.
     */
    public List<String> getDestinations() {
        return maildrop != null ? Collections.unmodifiableList(maildrop) : Collections.emptyList();
    }

    /**
     * Method to set the maildrop destinations.
     * This replaces the current maildrop with a new list of destinations.
     * @param destinations the new list of destination email addresses.
     */
    public void setDestinations(List<String> destinations) {
        this.maildrop = new ArrayList<>(destinations);
        updateLastChange();
    }

    /**
     * Method to update the last change timestamp to the current time.
     * This sets the lastChange attribute to the current epoch second.
     */
    public void updateLastChange() {
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
    }

    /**
     * Convenience method to get the last change timestamp as a Long.
     * This parses the lastChange string to a Long, handling potential format issues.
     * @return the last change timestamp as a Long, or null if it cannot be parsed.
     */
    public Long getLastChangeAsLong() {
        try {
            return lastChange != null ? Long.valueOf(lastChange) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convenience method to set the last change timestamp from a Long.
     * This converts the Long to a String and sets it as the lastChange attribute.
     * @param lastChange the last change timestamp as a Long.
     */
    public void setLastChangeAsLong(Long lastChange) {
        this.lastChange = lastChange != null ? String.valueOf(lastChange) : null;
    }

    // Getters and Setters - return/accept Strings for LDAP compatibility
    public Name getId() { return id; }
    public void setId(Name id) { this.id = id; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public List<String> getMaildrop() { return maildrop; }
    public void setMaildrop(List<String> maildrop) { this.maildrop = maildrop; }

    public String getAccountActive() { return accountActive; }
    public void setAccountActive(String accountActive) { this.accountActive = accountActive; }

    public String getLastChange() { return lastChange; }
    public void setLastChange(String lastChange) { this.lastChange = lastChange; }

    public String getMailsource() { return mailsource; }
    public void setMailsource(String mailsource) { this.mailsource = mailsource; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }

    public List<String> getRoleOccupant() { return roleOccupant; }
    public void setRoleOccupant(List<String> roleOccupant) {
        // Ensure at least one role occupant
        if (roleOccupant == null || roleOccupant.isEmpty()) {
            this.roleOccupant = new ArrayList<>();
            this.roleOccupant.add("cn=admin,o=hosting,dc=example,dc=com");
        } else {
            this.roleOccupant = roleOccupant;
        }
    }

    @Override
    public String toString() {
        return "JammPostmaster{" +
                "mail='" + mail + '\'' +
                ", accountActive='" + accountActive + '\'' +
                ", roleOccupants=" + (roleOccupant != null ? roleOccupant.size() : 0) +
                ", destinations=" + (maildrop != null ? maildrop.size() : 0) +
                '}';
    }
}