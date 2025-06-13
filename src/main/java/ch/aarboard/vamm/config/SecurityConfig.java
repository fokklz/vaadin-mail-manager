package ch.aarboard.vamm.config;

import ch.aarboard.vamm.security.LdapAuthenticationProvider;
import ch.aarboard.vamm.ui.views.security.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, LoginView.class);

        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/domains", true)
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );
    }

    @Bean
    public LdapConfig ldapConfig() {
        return new LdapConfig();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(@Autowired ApplicationContext applicationContext) {
        return new LdapAuthenticationProvider(ldapConfig(), applicationContext);
    }
}