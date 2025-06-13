package ch.aarboard.vamm.services;

import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.data.repositories.JammMailAccountRepository;
import ch.aarboard.vamm.data.repositories.JammMailAliasRepository;
import ch.aarboard.vamm.data.repositories.JammVirtualDomainRepository;
import ch.aarboard.vamm.utils.MailUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JammMailAliasManagemeentService {

    public JammMailAliasRepository mailAliasRepository;
    public JammMailAccountRepository mailAccountRepository;
    public JammVirtualDomainRepository virtualDomainRepository;

    public JammMailAliasManagemeentService(@Lazy JammMailAliasRepository mailAliasRepository,
                                           @Lazy JammMailAccountRepository mailAccountRepository,
                                           @Lazy JammVirtualDomainRepository virtualDomainRepository) {
        this.mailAliasRepository = mailAliasRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.virtualDomainRepository = virtualDomainRepository;
    }

    public int getAliasCount(String domainName) {
        validateDomainExists(domainName);
        return mailAliasRepository.countByDomain(domainName);
    }

    public List<JammMailAlias> getAliasesByDomain(String domainName) {
        validateDomainExists(domainName);
        return mailAliasRepository.findByDomain(domainName);
    }

    public JammMailAlias getAlias(String aliasEmail) {
        var aliasOpt = mailAliasRepository.findByEmail(aliasEmail);
        if (aliasOpt.isEmpty()) {
            throw new IllegalArgumentException("Alias not found: " + aliasEmail);
        }
        return aliasOpt.get();
    }

    public JammMailAlias getCatchAllAlias(String domainName) {
        validateDomainExists(domainName);
        var catchAllOpt = mailAliasRepository.findCatchAllByDomain(domainName);
        if (catchAllOpt.isEmpty()) {
            throw new IllegalArgumentException("Catch-all alias not found for domain: " + domainName);
        }

        return catchAllOpt.get();
    }

    public JammMailAlias createAlias(String aliasEmail, List<String> destinations, String description) {
        // Validate alias email format
        if (!MailUtils.isValidAddress(aliasEmail)) {
            throw new IllegalArgumentException("Invalid alias email format: " + aliasEmail);
        }

        // Check if alias already exists
        if (mailAliasRepository.existsByEmail(aliasEmail)) {
            throw new IllegalArgumentException("Alias already exists: " + aliasEmail);
        } else if (mailAccountRepository.existsByEmail(aliasEmail)) {
            throw new IllegalArgumentException("Alias cannot be the same as an existing account: " + aliasEmail);
        }

        // Extract domain and validate it exists
        String domain = MailUtils.extractDomainFromMail(aliasEmail);
        validateDomainExists(domain);

        // Validate destinations
        if (destinations == null || destinations.isEmpty()) {
            throw new IllegalArgumentException("Alias must have at least one destination");
        }

        for (String destination : destinations) {
            if (!MailUtils.isValidAddress(destination)) {
                throw new IllegalArgumentException("Invalid destination email format: " + destination);
            }
        }

        // Create alias
        JammMailAlias alias = new JammMailAlias(aliasEmail, destinations, MailUtils.extractUserFromMail(aliasEmail));

        if (description != null && !description.trim().isEmpty()) {
            alias.setDescription(description.trim());
        }

        // Set common name
        alias.setCommonName(MailUtils.extractUserFromMail(aliasEmail));

        return mailAliasRepository.save(alias);
    }

    public JammMailAlias createCatchAllAlias(String domainName, List<String> destinations, String description) {
        validateDomainExists(domainName);

        String catchAllEmail = "@" + domainName;

        // Check if catch-all already exists
        if (mailAliasRepository.existsByEmail(catchAllEmail)) {
            throw new IllegalArgumentException("Catch-all alias already exists for domain: " + domainName);
        }

        return createAlias(catchAllEmail, destinations, description);
    }

    public JammMailAlias updateAlias(JammMailAlias alias) {
        if (!mailAliasRepository.existsByEmail(alias.getMail())) {
            throw new IllegalArgumentException("Alias not found: " + alias.getMail());
        }

        alias.updateLastChange();
        return mailAliasRepository.save(alias);
    }

    public JammMailAlias addDestination(String aliasEmail, String destination) {
        if (!MailUtils.isValidAddress(destination)) {
            throw new IllegalArgumentException("Invalid destination email format: " + destination);
        }
        JammMailAlias alias = getAlias(aliasEmail);
        alias.addDestination(destination);

        return mailAliasRepository.save(alias);
    }

    public JammMailAlias removeDestination(String aliasEmail, String destination) {
        JammMailAlias alias = getAlias(aliasEmail);

        if (!alias.removeDestination(destination)) {
            throw new IllegalArgumentException("Destination not found in alias: " + destination);
        }

        // Don't allow removing all destinations
        if (alias.getDestinations().isEmpty()) {
            throw new IllegalArgumentException("Alias must have at least one destination");
        }

        return mailAliasRepository.save(alias);
    }

    public JammMailAlias toggleAliasStatus(String aliasEmail) {
        JammMailAlias alias = getAlias(aliasEmail);

        alias.setActive(!alias.isActive());
        alias.updateLastChange();

        return mailAliasRepository.save(alias);
    }

    public void deleteAlias(String aliasEmail) {
        if (!mailAliasRepository.existsByEmail(aliasEmail)) {
            throw new IllegalArgumentException("Alias not found: " + aliasEmail);
        }

        mailAliasRepository.deleteByEmail(aliasEmail);
    }

    public void deleteAllAliasesInDomain(String domainName) {
        validateDomainExists(domainName);
        mailAliasRepository.deleteAllByDomain(domainName);
    }


    private void validateDomainExists(String domainName) {
        if (!virtualDomainRepository.existsByName(domainName)) {
            throw new IllegalArgumentException("Domain not found: " + domainName);
        }
    }

}
