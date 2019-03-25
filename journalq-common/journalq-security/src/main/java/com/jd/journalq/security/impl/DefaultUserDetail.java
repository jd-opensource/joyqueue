package com.jd.journalq.security.impl;

import com.jd.journalq.security.UserDetails;

/**
 * @author majun8
 */
public class DefaultUserDetail implements UserDetails {

    private String user;
    private String password;
    private boolean admin;

    public DefaultUserDetail(String user, String password) {
        this(user, password, false);
    }

    public DefaultUserDetail(String user, String password, boolean admin) {
        this.user = user;
        this.password = password;
        this.admin = admin;
    }

    @Override
    public String getUsername() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }
}
