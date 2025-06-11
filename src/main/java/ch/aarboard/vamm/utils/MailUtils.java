package ch.aarboard.vamm.utils;

public class MailUtils {

    public static String extractDomainFromMail(String mail) {
        if (mail != null && mail.contains("@")) {
            return mail.substring(mail.indexOf("@") + 1);
        }
        return "";
    }

    public static String extractUserFromMail(String mail) {
        if (mail != null && mail.contains("@")) {
            return mail.substring(0, mail.indexOf("@"));
        }
        return mail;
    }
}
