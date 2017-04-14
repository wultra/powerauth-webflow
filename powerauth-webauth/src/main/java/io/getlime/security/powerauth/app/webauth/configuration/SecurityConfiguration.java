package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .antMatcher("/**")
                    .authorizeRequests()
                .antMatchers("/", "/webauth/**", "/built/**", "/css/**", "/images/**").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"));
    }
}
