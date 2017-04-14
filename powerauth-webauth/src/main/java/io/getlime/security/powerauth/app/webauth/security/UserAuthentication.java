package io.getlime.security.powerauth.app.webauth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Object representing a user authentication.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UserAuthentication extends AbstractAuthenticationToken implements Serializable {

    private static final long serialVersionUID = -3790516505615465445L;

    private String userId;

    /**
     * Default constructor
     */
    public UserAuthentication() {
        super(null);
    }

    /**
     * Constructor for a new PowerAuthApiAuthenticationImpl
     * @param activationId Activation ID
     * @param userId User ID
     */
    public UserAuthentication(String activationId, String userId) {
        super(null);
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority("USER"));
        return Collections.unmodifiableList(authorities);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.userId;
    }

    /**
     * Get user ID
     * @return User ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID
     * @param userId User ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
