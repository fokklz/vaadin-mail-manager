package ch.aarboard.vamm.utils;

public class MailUtils {



    public static String extractDomainFromMail(String mail) {
        if (mail != null && mail.contains("@")) {
            return mail.substring(mail.indexOf("@") + 1);
        }
        return "";
    }

    public static String extractUserFromMail(String mail) {
        // Special case for catch-all aliases
        if (mail.startsWith("@")) {
            return "catch-all";
        }

        if (mail != null && mail.contains("@")) {
            return mail.substring(0, mail.indexOf("@"));
        }
        return mail;
    }

    /**
     * Validates if an email address has a valid format.
     * Basic validation - checks for presence of @ and non-empty parts.
     *
     * @param address Email address to validate
     * @return true if address appears valid, false otherwise
     */
    public static boolean isValidAddress(String address) {
        if (address.startsWith("@") && address.indexOf("@", 1) == -1) {
            return true; // Catch-all alias format
        }

        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        int atIndex = address.indexOf("@");
        if (atIndex <= 0 || atIndex >= address.length() - 1) {
            return false;
        }

        // Check for multiple @ symbols
        if (address.indexOf("@", atIndex + 1) != -1) {
            return false;
        }

        String user = address.substring(0, atIndex);
        String domain = address.substring(atIndex + 1);

        return !user.trim().isEmpty() && !domain.trim().isEmpty();
    }

    public static boolean isValidQuotaFormat(String quota) {
        if (quota == null || quota.trim().isEmpty()) {
            return true; // Allow empty quota
        }

        // Basic quota validation: number followed by optional unit (K, M, G)
        return quota.matches("^\\d+[KMG]?$");
    }

    public static boolean isValidDomainName(String domainName) {
        if (domainName == null || domainName.trim().isEmpty()) {
            return false;
        }

        // Basic regex for domain validation
        return domainName.matches("^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]?\\.[a-zA-Z]{2,}$");
    }
}
