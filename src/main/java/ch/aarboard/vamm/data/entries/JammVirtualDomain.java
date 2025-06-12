package ch.aarboard.vamm.data.entries;

import ch.aarboard.vamm.utils.LdapUtils;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;

@Entry(objectClasses = {"top", "JammVirtualDomain"})
public final class JammVirtualDomain {

    @Id
    private Name id;

    @Attribute(name = "jvd")
    private String jvd;

    // Boolean
    @Attribute(name = "accountActive")
    private String accountActive;

    // Long
    @Attribute(name = "lastChange")
    private String lastChange;

    // Boolean
    @Attribute(name = "delete")
    private String delete;

    // Boolean
    @Attribute(name = "editAccounts")
    private String editAccounts;

    // Boolean
    @Attribute(name = "editPostmasters")
    private String editPostmasters;

    @Attribute(name = "postfixTransport")
    private String postfixTransport;

    @Attribute(name = "description")
    private String description;

    @Transient
    private int accountCount = 0;

    @Transient
    private int aliasCount = 0;

    // Constructors
    public JammVirtualDomain() {
        this.accountActive = "TRUE";
        this.delete = "FALSE";
        this.editAccounts = "TRUE";
        this.editPostmasters = "TRUE";
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
        this.postfixTransport = "virtual:";
    }

    public JammVirtualDomain(String domainName) {
        this();
        this.jvd = domainName;
        this.id = LdapUtils.domainDN(domainName).build();
    }

    /**
     * Mark the domain for deletion.
     * This sets the delete flag to "TRUE" and deactivates the account.
     */
    public void markForDeletion() {
        setMarkedForDeletion(true);
        setActive(false);
        updateLastChange();
    }

    /**
     * Activate the domain.
     * This sets the accountActive flag to "TRUE" and resets the delete flag.
     */
    public void activate() {
        setMarkedForDeletion(false);
        setActive(true);
        updateLastChange();
    }

    /**
     * Deactivate the domain.
     * This sets the accountActive flag to "FALSE" and updates the last change timestamp.
     */
    public void deactivate() {
        setActive(false);
        updateLastChange();
    }

    /**
     * Check if the domain is active.
     * @return true if the domain is active, false otherwise.
     */
    public boolean isActive() {
        return "TRUE".equals(accountActive);
    }

    /**
     * Set the active status of the domain.
     * @param active true to activate, false to deactivate.
     */
    public void setActive(boolean active) {
        this.accountActive = active ? "TRUE" : "FALSE";
    }

    /**
     * Check if the domain is marked for deletion.
     * @return true if the domain is marked for deletion, false otherwise.
     */
    public boolean isMarkedForDeletion() {
        return "TRUE".equals(delete);
    }

    /**
     * Set the domain marked for deletion status.
     * @param deleted true to mark the domain for deletion, false to unmark.
     */
    public void setMarkedForDeletion(boolean deleted) {
        this.delete = deleted ? "TRUE" : "FALSE";
    }

    /**
     * Check if the domain allows editing of accounts.
     * @return true if accounts can be edited, false otherwise.
     */
    public boolean canEditAccounts() {
        return "TRUE".equals(editAccounts);
    }

    /**
     * Set whether the domain allows editing of accounts.
     * @param canEdit true to allow editing, false to disallow.
     */
    public void setCanEditAccounts(boolean canEdit) {
        this.editAccounts = canEdit ? "TRUE" : "FALSE";
    }

    /**
     * Check if the domain allows editing of postmasters.
     * @return true if postmasters can be edited, false otherwise.
     */
    public boolean canEditPostmasters() {
        return "TRUE".equals(editPostmasters);
    }

    /**
     * Set whether the domain allows editing of postmasters.
     * @param canEdit true to allow editing, false to disallow.
     */
    public void setCanEditPostmasters(boolean canEdit) {
        this.editPostmasters = canEdit ? "TRUE" : "FALSE";
    }

    /**
     * Update the last change timestamp to the current time.
     * This is typically called when any significant change is made to the domain.
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

    // Getters and Setters - all return/accept Strings to match LDAP format
    public Name getId() { return id; }
    public void setId(Name id) { this.id = id; }

    public String getJvd() { return jvd; }
    public void setJvd(String jvd) { this.jvd = jvd; }

    public String getAccountActive() { return accountActive; }
    public void setAccountActive(String accountActive) { this.accountActive = accountActive; }

    public String getLastChange() { return lastChange; }
    public void setLastChange(String lastChange) { this.lastChange = lastChange; }

    public String getDelete() { return delete; }
    public void setDelete(String delete) { this.delete = delete; }

    public String getEditAccounts() { return editAccounts; }
    public void setEditAccounts(String editAccounts) { this.editAccounts = editAccounts; }

    public String getEditPostmasters() { return editPostmasters; }
    public void setEditPostmasters(String editPostmasters) { this.editPostmasters = editPostmasters; }

    public String getPostfixTransport() { return postfixTransport; }
    public void setPostfixTransport(String postfixTransport) { this.postfixTransport = postfixTransport; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Transient fields
    public int getAccountCount() { return accountCount; }
    public void setAccountCount(int accountCount) { this.accountCount = accountCount; }

    public int getAliasCount() { return aliasCount; }
    public void setAliasCount(int aliasCount) { this.aliasCount = aliasCount; }

    @Override
    public String toString() {
        return "JammVirtualDomain{" +
                "jvd='" + jvd + '\'' +
                ", accountActive='" + accountActive + '\'' +
                ", delete='" + delete + '\'' +
                ", editAccounts='" + editAccounts + '\'' +
                ", editPostmasters='" + editPostmasters + '\'' +
                ", lastChange='" + lastChange + '\'' +
                ", postfixTransport='" + postfixTransport + '\'' +
                ", description='" + description + '\'' +
                ", accountCount=" + accountCount +
                ", aliasCount=" + aliasCount +
                '}';
    }
}