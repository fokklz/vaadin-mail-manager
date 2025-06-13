package ch.aarboard.vamm.services;

import ch.aarboard.vamm.data.entries.JammPostmaster;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import ch.aarboard.vamm.data.repositories.JammMailAccountRepository;
import ch.aarboard.vamm.data.repositories.JammMailAliasRepository;
import ch.aarboard.vamm.data.repositories.JammPostmasterRepository;
import ch.aarboard.vamm.data.repositories.JammVirtualDomainRepository;
import ch.aarboard.vamm.utils.MailUtils;
import org.springframework.context.annotation.Lazy;

import java.util.List;

public class JammVirtualDomainManagementService {

    private JammVirtualDomainRepository virtualDomainRepository;
    private JammMailAccountRepository mailAccountRepository;
    private JammMailAliasRepository mailAliasRepository;
    private JammPostmasterRepository postmasterRepository;

    public JammVirtualDomainManagementService(
            @Lazy JammVirtualDomainRepository virtualDomainRepository,
            @Lazy JammMailAccountRepository mailAccountRepository,
            @Lazy JammMailAliasRepository mailAliasRepository,
            @Lazy JammPostmasterRepository postmasterRepository) {
        this.virtualDomainRepository = virtualDomainRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.mailAliasRepository = mailAliasRepository;
        this.postmasterRepository = postmasterRepository;
    }

    public int getDomainCount() {
        return getAllDomains().size();
    }

    public boolean domainExists(String domainName) {
        return virtualDomainRepository.existsByName(domainName);
    }

    public List<JammVirtualDomain> getAllDomains() {
        return virtualDomainRepository.findAll();
    }

    public List<JammVirtualDomain> getAllDomainsWithStats() {
        List<JammVirtualDomain> domains = virtualDomainRepository.findAll();

        // Populate statistics for each domain
        for (JammVirtualDomain domain : domains) {
            String domainName = domain.getJvd();
            domain.setAccountCount(mailAccountRepository.countByDomain(domainName));
            domain.setAliasCount(mailAliasRepository.countByDomainExcludingSystem(domainName));
        }

        return domains;
    }

    public JammVirtualDomain getDomainNoStats(String domainName) {
        var domainOpt = virtualDomainRepository.findByName(domainName);
        if (domainOpt.isEmpty()) {
            throw new IllegalArgumentException("Domain not found: " + domainName);
        }
        return domainOpt.get();
    }

    public JammVirtualDomain getDomain(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);

        // Populate statistics
        domain.setAccountCount(mailAccountRepository.countByDomain(domainName));
        domain.setAliasCount(mailAliasRepository.countByDomainExcludingSystem(domainName));

        return domain;
    }

    public JammVirtualDomain createDomain(String domainName, String description) {
        // Check if domain already exists
        if (virtualDomainRepository.existsByName(domainName)) {
            throw new IllegalArgumentException("Domain already exists: " + domainName);
        }

        // Validate domain name format
        if (!MailUtils.isValidDomainName(domainName)) {
            throw new IllegalArgumentException("Invalid domain name format: " + domainName);
        }

        // Create domain
        JammVirtualDomain domain = new JammVirtualDomain(domainName);

        // Handle null or empty description - LDAP requires empty string, not null
        if (description != null && !description.trim().isEmpty()) {
            domain.setDescription(description.trim());
        }

        domain = virtualDomainRepository.save(domain);

        // Create default postmaster
        JammPostmaster postmaster = new JammPostmaster(domainName);
        postmaster.setDescription("Default postmaster for " + domainName);
        postmasterRepository.save(postmaster);

        return domain;
    }

    public JammVirtualDomain updateDomain(JammVirtualDomain domain) {
        if (!virtualDomainRepository.existsByName(domain.getJvd())) {
            throw new IllegalArgumentException("Domain not found: " + domain.getJvd());
        }

        return virtualDomainRepository.save(domain);
    }

    public JammVirtualDomain toggleDomainStatus(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);
        if (domain.isActive()) {
            domain.deactivate();
        } else {
            domain.activate();
        }

        return virtualDomainRepository.save(domain);
    }

    public JammVirtualDomain activateDomain(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);
        if (domain.isActive()) {
            throw new IllegalArgumentException("Domain is already active: " + domainName);
        }

        domain.activate();
        return virtualDomainRepository.save(domain);
    }

    public JammVirtualDomain deactivateDomain(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);
        if (!domain.isActive()) {
            throw new IllegalArgumentException("Domain is already inactive: " + domainName);
        }

        domain.deactivate();
        return virtualDomainRepository.save(domain);
    }

    public JammVirtualDomain markDomainForDeletion(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);
        if (domain.isMarkedForDeletion()) {
            throw new IllegalArgumentException("Domain is already marked for deletion: " + domainName);
        }

        domain.setMarkedForDeletion(true);
        return virtualDomainRepository.save(domain);
    }

    public JammVirtualDomain unmarkDomainForDeletion(String domainName) {
        JammVirtualDomain domain = getDomainNoStats(domainName);
        if (!domain.isMarkedForDeletion()) {
            throw new IllegalArgumentException("Domain is not marked for deletion: " + domainName);
        }

        domain.setMarkedForDeletion(false);
        return virtualDomainRepository.save(domain);
    }

    public void deleteDomain(String domainName) {
        if (!virtualDomainRepository.existsByName(domainName)) {
            throw new IllegalArgumentException("Domain not found: " + domainName);
        }

        // Delete all associated data
        mailAccountRepository.deleteAllByDomain(domainName);
        mailAliasRepository.deleteAllByDomain(domainName);
        postmasterRepository.deleteByDomain(domainName);

        // Finally delete the domain
        virtualDomainRepository.deleteByName(domainName);
    }
}
