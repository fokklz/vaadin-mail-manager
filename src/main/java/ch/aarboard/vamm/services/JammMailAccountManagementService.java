package ch.aarboard.vamm.services;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.repositories.JammMailAccountRepository;
import ch.aarboard.vamm.data.repositories.JammMailAliasRepository;
import ch.aarboard.vamm.data.repositories.JammVirtualDomainRepository;
import ch.aarboard.vamm.utils.MailUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JammMailAccountManagementService {

    private JammMailAccountRepository mailAccountRepository;
    private JammMailAliasRepository mailAliasRepository;
    private JammVirtualDomainRepository virtualDomainRepository;

    public JammMailAccountManagementService(@Lazy JammMailAccountRepository mailAccountRepository,
                                            @Lazy JammMailAliasRepository mailAliasRepository,
                                            @Lazy JammVirtualDomainRepository virtualDomainRepository) {
        this.mailAccountRepository = mailAccountRepository;
        this.mailAliasRepository = mailAliasRepository;
        this.virtualDomainRepository = virtualDomainRepository;
    }

    public int getAccountCount(String domainName) {
        validateDomainExists(domainName);
        return mailAccountRepository.countByDomain(domainName);
    }

    public List<JammMailAccount> getAllAccounts() {
        return mailAccountRepository.findAll();
    }

    public List<JammMailAccount> getAccountsByDomain(String domainName) {
        validateDomainExists(domainName);
        return mailAccountRepository.findByDomain(domainName);
    }

    public JammMailAccount getAccount(String email) {
        var accountOpt = mailAccountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + email);
        }

        return accountOpt.get();
    }

    public JammMailAccount createAccount(String email, String password, String homeDirectory,
                                         String quota, String description) {
        // Validate email format
        if (!MailUtils.isValidAddress(email)) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }

        // Check if account already exists
        if (mailAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Account already exists: " + email);
        } else if (mailAliasRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already used as an alias: " + email);
        }

        // Extract domain and validate it exists
        String domain = MailUtils.extractDomainFromMail(email);
        validateDomainExists(domain);

        // Create account with required fields
        String accountName = MailUtils.extractUserFromMail(email);
        String defaultHomeDir = homeDirectory != null ? homeDirectory : "/var/vmail/" + domain + "/" + accountName;
        String defaultMailbox = defaultHomeDir + "/";

        JammMailAccount account = new JammMailAccount(email, defaultHomeDir, defaultMailbox);

        if (password != null && !password.trim().isEmpty()) {
            account.setPasswordSecure(password);
        }

        if (quota != null && !quota.trim().isEmpty()) {
            account.setQuota(quota);
        }

        if (description != null && !description.trim().isEmpty()) {
            account.setDescription(description);
        }

        account.setCommonName(accountName);
        account.setUid(accountName);

        return mailAccountRepository.save(account);
    }

    public JammMailAccount updateAccount(JammMailAccount account) {
        if (!mailAccountRepository.existsByEmail(account.getMail())) {
            throw new IllegalArgumentException("Account not found: " + account.getMail());
        }

        account.updateLastChange();
        return mailAccountRepository.save(account);
    }

    public JammMailAccount changePassword(String email, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        JammMailAccount account = getAccount(email);

        account.setPasswordSecure(newPassword);
        account.updateLastChange();

        return mailAccountRepository.save(account);
    }

    public JammMailAccount setAccountQuota(String email, String quota) {
        // Validate quota format (e.g., "100M", "1G", etc.)
        if (quota != null && !quota.trim().isEmpty() && !MailUtils.isValidQuotaFormat(quota)) {
            throw new IllegalArgumentException("Invalid quota format: " + quota);
        }

        JammMailAccount account = getAccount(email);

        account.setQuota(quota);
        account.updateLastChange();

        return mailAccountRepository.save(account);
    }

    public JammMailAccount toggleAccountStatus(String email) {
        JammMailAccount account = getAccount(email);

        account.setActive(!account.isActive());
        account.updateLastChange();

        return mailAccountRepository.save(account);
    }

    public JammMailAccount markAccountForDeletion(String email) {
        JammMailAccount account = getAccount(email);

        account.setMarkedForDeletion(true);
        account.setActive(false);
        account.updateLastChange();

        return mailAccountRepository.save(account);
    }

    public JammMailAccount restoreAccountFromDeletion(String email) {
        JammMailAccount account = getAccount(email);
        account.setMarkedForDeletion(false);
        account.setActive(true);
        account.updateLastChange();

        return mailAccountRepository.save(account);
    }

    public void deleteAccount(String email) {
        if (!mailAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Account not found: " + email);
        }

        mailAccountRepository.deleteByEmail(email);
    }

    public void deleteAllAccountsInDomain(String domainName) {
        validateDomainExists(domainName);
        mailAccountRepository.deleteAllByDomain(domainName);
    }

    private void validateDomainExists(String domainName) {
        if (!virtualDomainRepository.existsByName(domainName)) {
            throw new IllegalArgumentException("Domain not found: " + domainName);
        }
    }


}
