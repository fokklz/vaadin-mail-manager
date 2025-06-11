package ch.aarboard.vamm.security;

import ch.aarboard.vamm.config.LdapConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LdapAuthenticationProvider implements AuthenticationProvider {

    private LdapConfig ldapConfig;
    private ApplicationContext applicationContext;

    public LdapAuthenticationProvider(LdapConfig ldapConfig, @Autowired ApplicationContext applicationContext) {
        this.ldapConfig = ldapConfig;
        this.applicationContext = applicationContext;
    }

    private Authentication createAuthenticationToken(String username, String password, List<SimpleGrantedAuthority> authorities, String userDn) {
        Map<String, Object> details = new HashMap<>();
        details.put("password", password);
        details.put("userDn", userDn);

        var token = new UsernamePasswordAuthenticationToken(username, password, authorities);
        token.setDetails(details);
        return token;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            // Handle root user specially
            if (username.equals("root")) {
                return authenticateRoot(username, password);
            }

            throw new BadCredentialsException("Invalid credentials");

        } catch (Exception e) {
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean authenticateWithDn(String userDn, String password) {
        try {
            LdapContextSource testSource = new LdapContextSource();
            testSource.setUrl(ldapConfig.getUrl());
            testSource.setBase(ldapConfig.getBase());
            testSource.setUserDn(userDn);
            testSource.setPassword(password);
            testSource.afterPropertiesSet();

            // Test the bind - if this succeeds, credentials are valid
            var context = testSource.getReadOnlyContext();
            System.out.println("LDAP bind successful for DN: " + userDn);
            context.close();
            return true;

        } catch (Exception e) {
            System.err.println("LDAP bind failed for DN: " + userDn);
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            return false;
        }
    }

    private Authentication authenticateRoot(String username, String password) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        var userDn = "cn=admin,dc=example,dc=com";

        authorities.add(new SimpleGrantedAuthority("ROLE_SITE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_DOMAIN_ADMIN"));

        if (authenticateWithDn(userDn, password)){
            return createAuthenticationToken(username, password, authorities, userDn);
        } else {
            throw new BadCredentialsException("Invalid root credentials");
        }
    }
}
