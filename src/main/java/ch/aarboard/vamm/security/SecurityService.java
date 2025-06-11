package ch.aarboard.vamm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public class SecurityService {

    /**
     * Get the current authentication object
     */
    public Optional<Authentication> getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(auth);
    }

    /**
     * Get the current authenticated username
     */
    public Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Get the current user's DN from authentication details
     */
    public Optional<String> getCurrentUserDn() {
        return getCurrentAuthentication()
                .map(Authentication::getDetails)
                .filter(details -> details instanceof Map)
                .map(details -> (Map<?, ?>) details)
                .map(detailsMap -> (String) detailsMap.get("userDn"));
    }

    /**
     * Get the current user's password from authentication details
     */
    public Optional<String> getCurrentUserPassword() {
        return getCurrentAuthentication()
                .map(Authentication::getDetails)
                .filter(details -> details instanceof Map)
                .map(details -> (Map<?, ?>) details)
                .map(detailsMap -> (String) detailsMap.get("password"));
    }

    /**
     * Check if the current user is authenticated
     */
    public boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Check if the current user has a specific role
     */
    public boolean hasRole(String role) {
        return hasAuthority(role);
    }

    /**
     * Check if the current user has a specific authority
     */
    public boolean hasAuthority(String authority) {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equalsIgnoreCase(authority));
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user has all of the specified roles
     */
    public boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the current user is a site admin
     */
    public boolean isSiteAdmin() {
        return hasRole("ROLE_SITE_ADMIN");
    }

    /**
     * Check if the current user is a domain admin (postmaster)
     */
    public boolean isDomainAdmin() {
        return hasRole("ROLE_DOMAIN_ADMIN");
    }

    /**
     * Check if the current user is a regular user
     */
    public boolean isUser() {
        return hasRole("ROLE_USER");
    }

    /**
     * Check if the current user is root
     */
    public boolean isRoot() {
        return getCurrentUsername()
                .map("root"::equals)
                .orElse(false);
    }

    /**
     * Check if the current user can modify the specified email account
     * - Site admins can modify any account
     * - Domain admins can modify accounts in their domains
     * - Regular users can only modify their own account
     */
    public boolean canModifyAccount(String email) {
        if (isSiteAdmin()) {
            return true;
        }

        if (isDomainAdmin()) {
            // Domain admins can modify accounts in their domains
            // TODO: implement check
            return true;
        }

        if (isUser()) {
            // Regular users can only modify their own account
            return getCurrentUsername()
                    .map(username -> username.equals(email))
                    .orElse(false);
        }

        return false;
    }

    /**
     * Check if the current user can access a specific domain
     * - Site admins can access any domain
     * - Domain admins can access their managed domains
     * - Regular users can access their own domain
     */
    public boolean canAccessDomain(String domain) {
        if (isSiteAdmin()) {
            return true;
        }

        if (isDomainAdmin()) {
            // Domain admins can access their domain
            // TODO: implement check
            return true;
        }

        if (isUser()) {
            // Regular users can access their own domain
            return getCurrentUsername()
                    .map(email -> email.contains("@") ? email.substring(email.indexOf("@") + 1) : "")
                    .map(userDomain -> userDomain.equals(domain))
                    .orElse(false);
        }

        return false;
    }

    /**
     * Get all authorities of the current user
     */
    public Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(java.util.Collections.emptyList());
    }

    /**
     * Clear the security context (logout)
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Get the user's email domain
     */
    public Optional<String> getCurrentUserDomain() {
        return getCurrentUsername()
                .filter(email -> email.contains("@"))
                .map(email -> email.substring(email.indexOf("@") + 1));
    }

    /**
     * Check if the current user is the owner of the specified email
     */
    public boolean isOwnerOf(String email) {
        return getCurrentUsername()
                .map(username -> username.equals(email))
                .orElse(false);
    }

    /**
     * Get user role hierarchy level (higher number = more privileges)
     */
    public int getUserRoleLevel() {
        if (isSiteAdmin()) return 3;
        if (isDomainAdmin()) return 2;
        if (isUser()) return 1;
        return 0;
    }

    /**
     * Check if current user has sufficient privileges to perform an action
     * requiring a specific role level
     */
    public boolean hasMinimumRoleLevel(int requiredLevel) {
        return getUserRoleLevel() >= requiredLevel;
    }
}