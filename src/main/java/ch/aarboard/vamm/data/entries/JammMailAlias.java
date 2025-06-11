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
 * LDAP entity representing a Jamm mail alias.
 * Maps to the JammMailAlias objectClass.
 */
@Entry(objectClasses = {"top", "JammMailAlias"})
public final class JammMailAlias {

    @Id
    private Name id;

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


    public JammMailAlias() {
        this.accountActive = "TRUE";
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
        this.maildrop = new ArrayList<>();
    }

    public JammMailAlias(String mail, List<String> destinations) {
        this();
        this.mail = mail;
        this.maildrop = new ArrayList<>(destinations);
        // Auto-generate DN based on mail and domain
        String domain = MailUtils.extractDomainFromMail(mail);
        this.id = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", domain)
                .add("mail", mail)
                .build();
    }

    public JammMailAlias(String mail, String... destinations) {
        this();
        this.mail = mail;
        this.maildrop = new ArrayList<>();
        Collections.addAll(this.maildrop, destinations);
        // Auto-generate DN based on mail and domain
        String domain = MailUtils.extractDomainFromMail(mail);
        this.id = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", domain)
                .add("mail", mail)
                .build();
    }

    /**
     * Convenience method to check if the alias is active.
     * @return true if the alias is active, false otherwise.
     */
    public boolean isActive() {
        return "TRUE".equals(accountActive);
    }

    /**
     * Convenience method to set the active status of the alias.
     * @param active true to activate, false to deactivate.
     */
    public void setActive(boolean active) {
        this.accountActive = active ? "TRUE" : "FALSE";
    }

    /**
     * Convenience method to get the alias name from the mail.
     * @return the alias name extracted from the mail address.
     */
    public String getAliasName() {
        return MailUtils.extractUserFromMail(mail);
    }

    /**
     * Convenience method to get the domain from the mail address.
     * @return the domain extracted from the mail address.
     */
    public String getDomain() {
        return MailUtils.extractDomainFromMail(mail);
    }

    /**
     * Convenience method to check if the alias is a catch-all alias.
     * A catch-all alias starts with "@".
     * @return true if the alias is a catch-all, false otherwise.
     */
    public boolean isCatchAll() {
        return mail != null && mail.startsWith("@");
    }

    /**
     * Convenience method to check if the alias is a postmaster alias.
     * A postmaster alias typically has "postmaster" in the mail address.
     * @return true if the alias is a postmaster alias, false otherwise.
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
     * @param destination the destination to remove.
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
     * Get a read-only view of the maildrop destinations.
     * @return an unmodifiable list of destinations.
     */
    public List<String> getDestinations() {
        return maildrop != null ? Collections.unmodifiableList(maildrop) : Collections.emptyList();
    }

    /**
     * Set the maildrop destinations.
     * @param destinations the list of destinations to set.
     */
    public void setDestinations(List<String> destinations) {
        this.maildrop = new ArrayList<>(destinations);
        updateLastChange();
    }

    /**
     * Update the last change timestamp to the current time.
     * This is typically called when the alias is modified.
     */
    public void updateLastChange() {
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
    }

    /**
     * Convenience method to get the last change timestamp as a Long.
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

    @Override
    public String toString() {
        return "JammMailAlias{" +
                "mail='" + mail + '\'' +
                ", destinations=" + (maildrop != null ? maildrop.size() : 0) +
                ", accountActive='" + accountActive + '\'' +
                ", commonName='" + commonName + '\'' +
                '}';
    }
}