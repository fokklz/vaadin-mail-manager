package ch.aarboard.vamm.data.entries;

import ch.aarboard.vamm.utils.MailUtils;
import ch.aarboard.vamm.utils.PasswordUtils;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.time.Instant;

/**
 * LDAP entity representing a Jamm mail account.
 * Maps to the JammMailAccount objectClass.
 */
@Entry(objectClasses = {"top", "JammMailAccount"})
public final class JammMailAccount {

    @Id
    private Name id;

    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "homeDirectory")
    private String homeDirectory;

    @Attribute(name = "mailbox")
    private String mailbox;

    // Boolean
    @Attribute(name = "accountActive")
    private String accountActive;

    // Long
    @Attribute(name = "lastChange")
    private String lastChange;

    // Boolean
    @Attribute(name = "delete")
    private String delete;

    @Attribute(name = "uidNumber")
    private Integer uidNumber;

    @Attribute(name = "gidNumber")
    private Integer gidNumber;

    @Attribute(name = "uid")
    private String uid;

    @Attribute(name = "cn")
    private String commonName;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "quota")
    private String quota;

    @Attribute(name = "userPassword")
    private String userPassword;

    @Attribute(name = "clearPassword")
    private String clearPassword;

    public JammMailAccount() {
        this.accountActive = "TRUE";
        this.delete = "FALSE";
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
    }

    public JammMailAccount(String mail, String homeDirectory, String mailbox) {
        this();
        this.mail = mail;
        this.homeDirectory = homeDirectory;
        this.mailbox = mailbox;
        // Auto-generate DN based on mail and domain
        String domain = MailUtils.extractDomainFromMail(mail);
        this.id = LdapNameBuilder.newInstance()
                .add("o", "hosting")
                .add("jvd", domain)
                .add("mail", mail)
                .build();
    }


    /**
     * Set password securely using SSHA hashing
     * This method hashes the password and updates the last change timestamp.
     * @param plainPassword the plain text password to hash
     */
    public void setPasswordSecure(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.clearPassword = plainPassword;
        this.userPassword = PasswordUtils.hashPasswordSsha(plainPassword);
        updateLastChange();
    }

    /**
     * Set password with a specific scheme
     * This method allows setting the password with different hashing schemes.
     * @param plainPassword the plain text password to hash
     * @param scheme the password hashing scheme to use
     */
    public void setPasswordWithScheme(String plainPassword, PasswordUtils.PasswordScheme scheme) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.clearPassword = plainPassword;

        switch (scheme) {
            case PLAIN -> this.userPassword = PasswordUtils.PasswordScheme.PLAIN.getPrefix() + plainPassword;
            case SSHA -> this.userPassword = PasswordUtils.hashPasswordSsha(plainPassword);
            case SHA -> this.userPassword = PasswordUtils.hashPasswordSha(plainPassword);
            case MD5 -> this.userPassword = PasswordUtils.hashPasswordMd5(plainPassword);
        }

        updateLastChange();
    }

    /**
     * Verify the provided plain password against the stored hashed password.
     * This method uses the PasswordUtils to verify the password.
     * @param plainPassword the plain text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword) {
        if (userPassword == null || plainPassword == null) {
            return false;
        }
        return PasswordUtils.verifyPassword(userPassword, plainPassword);
    }

    /**
     * Check if the account is active.
     * @return true if the account is active, false otherwise
     */
    public boolean isActive() {
        return "TRUE".equals(accountActive);
    }

    /**
     * Set the account active status.
     * @param active true to activate the account, false to deactivate
     */
    public void setActive(boolean active) {
        this.accountActive = active ? "TRUE" : "FALSE";
    }

    /**
     * Check if the account is marked for deletion.
     * @return true if the account is marked for deletion, false otherwise
     */
    public boolean isMarkedForDeletion() {
        return "TRUE".equals(delete);
    }

    /**
     * Set the account marked for deletion status.
     * @param deleted true to mark the account for deletion, false to unmark
     */
    public void setMarkedForDeletion(boolean deleted) {
        this.delete = deleted ? "TRUE" : "FALSE";
    }

    /**
     * Get the full path to the mailbox.
     * This combines homeDirectory and mailbox into a single path.
     * @return the full path to the mailbox, or null if either part is missing
     */
    public String getFullPathToMailbox() {
        if (homeDirectory != null && mailbox != null) {
            return homeDirectory + "/" + mailbox;
        }
        return null;
    }

    /**
     * Get the account name (local part of the email).
     * This is a convenience method to extract the local part of the email address.
     * @return the local part of the email address, or the full email if it cannot be parsed
     */
    public String getAccountName() {
        if (mail != null && mail.contains("@")) {
            return mail.substring(0, mail.indexOf("@"));
        }
        return mail;
    }

    /**
     * Get the domain part of the email address.
     * This is a convenience method to extract the domain from the email address.
     * @return the domain part of the email address, or an empty string if it cannot be parsed
     */
    public String getDomain() {
        return MailUtils.extractDomainFromMail(mail);
    }

    /**
     * Update the last change timestamp to the current time.
     * This method sets the lastChange attribute to the current epoch second.
     */
    public void updateLastChange() {
        this.lastChange = String.valueOf(Instant.now().getEpochSecond());
    }

    /**
     * Convenience method to get the last change timestamp as a Long.
     * This method parses the lastChange string and returns it as a Long.
     * @return the last change timestamp as a Long, or null if it cannot be parsed
     */
    public Long getLastChangeAsLong() {
        try {
            return lastChange != null ? Long.valueOf(lastChange) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Set the last change timestamp from a Long value.
     * This method converts the Long to a String and sets it as the lastChange attribute.
     * @param lastChange the last change timestamp as a Long, or null to clear it
     */
    public void setLastChangeAsLong(Long lastChange) {
        this.lastChange = lastChange != null ? String.valueOf(lastChange) : null;
    }

    // Getters and Setters - return/accept Strings for LDAP compatibility
    public Name getId() { return id; }
    public void setId(Name id) { this.id = id; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getHomeDirectory() { return homeDirectory; }
    public void setHomeDirectory(String homeDirectory) { this.homeDirectory = homeDirectory; }

    public String getMailbox() { return mailbox; }
    public void setMailbox(String mailbox) { this.mailbox = mailbox; }

    public String getAccountActive() { return accountActive; }
    public void setAccountActive(String accountActive) { this.accountActive = accountActive; }

    public String getLastChange() { return lastChange; }
    public void setLastChange(String lastChange) { this.lastChange = lastChange; }

    public String getDelete() { return delete; }
    public void setDelete(String delete) { this.delete = delete; }

    public Integer getUidNumber() { return uidNumber; }
    public void setUidNumber(Integer uidNumber) { this.uidNumber = uidNumber; }

    public Integer getGidNumber() { return gidNumber; }
    public void setGidNumber(Integer gidNumber) { this.gidNumber = gidNumber; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQuota() { return quota; }
    public void setQuota(String quota) { this.quota = quota; }

    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }

    public String getClearPassword() { return clearPassword; }
    public void setClearPassword(String clearPassword) { this.clearPassword = clearPassword; }

    @Override
    public String toString() {
        return "JammMailAccount{" +
                "mail='" + mail + '\'' +
                ", accountActive='" + accountActive + '\'' +
                ", delete='" + delete + '\'' +
                ", commonName='" + commonName + '\'' +
                ", mailbox='" + mailbox + '\'' +
                '}';
    }
}