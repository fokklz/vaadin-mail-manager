package ch.aarboard.vamm.utils;

import org.springframework.ldap.support.LdapNameBuilder;

public class LdapUtils {

    public static final String JAMM_MAIL_ACCOUNT = "JammMailAccount";
    public static final String JAMM_MAIL_ALIAS = "JammMailAlias";
    public static final String JAMM_VIRTUAL_DOMAIN = "JammVirtualDomain";
    public static final String JAMM_POSTMASTER = "JammPostmaster";

    /**
     * Builds a base LDAP name for the application.
     *
     * @return LdapNameBuilder initialized with o=hosting
     */
    public static LdapNameBuilder baseDN() {
        return LdapNameBuilder.newInstance().add("o", "hosting");
    }

    /**
     * Builds a domain-specific LDAP name.
     *
     * @param domain the domain to add to the base DN
     * @return LdapNameBuilder with the domain added, or the base DN if the domain is null or empty
     */
    public static LdapNameBuilder domainDN(String domain) {
        if (domain == null || domain.isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }
        return baseDN().add("jvd", domain);
    }

    /**
     * Builds an LDAP name for a mail address within a specific domain.
     *
     * @param domain the domain to add to the base DN
     * @param mail   the mail address to add to the DN
     * @return LdapNameBuilder with the domain and mail added, or the base DN if the domain is null or empty
     */
    public static LdapNameBuilder mailDN(String domain, String mail) {
        if (domain == null || domain.isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }

        if (mail == null || mail.isEmpty()) {
            throw new IllegalArgumentException("Mail cannot be null or empty");
        }

        return domainDN(domain).add("mail", mail);
    }

    /**
     * Builds an LDAP name for a mail address within a specific domain.
     *
     * @param mail the mail address to add to the DN
     * @return LdapNameBuilder with the mail added, or the base DN if the mail is null or empty
     */
    public static LdapNameBuilder mailDN(String mail) {
        if (mail == null || mail.isEmpty()) {
            throw new IllegalArgumentException("Mail cannot be null or empty");
        }
        String domain = MailUtils.extractDomainFromMail(mail);
        return mailDN(domain, mail);
    }

    /**
     *
     * @param domain
     * @return
     */
    public static LdapNameBuilder postmasterDN(String domain) {
        if (domain == null || domain.isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }
        return domainDN(domain).add("cn", "postmaster");
    }

    /**
     * Escapes special characters in a search term for LDAP queries.
     * This method escapes characters that have special meaning in LDAP search filters.
     *
     * @param searchTerm the term to escape
     * @return the escaped search term, or null if the input is null
     */
    public static String escape(String searchTerm) {
        if (searchTerm == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : searchTerm.toCharArray()) {
            switch (c) {
                case '*':
                case '(':
                case ')':
                case '\\':
                case '\u0000':
                    escaped.append('\\');
                    // fall through
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }

}
