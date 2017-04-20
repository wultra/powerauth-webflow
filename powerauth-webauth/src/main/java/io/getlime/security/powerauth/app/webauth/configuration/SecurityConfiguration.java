package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER) // because of OAuth 2 security on some resources
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/authenticate", "/continue", "/oauth/error",  "/api/**", "/resources/**").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/authenticate"));
    }
}
