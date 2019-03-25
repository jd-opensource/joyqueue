package com.jd.journalq.broker;

import com.jd.journalq.response.BooleanResponse;
import com.jd.journalq.security.Authentication;
import com.jd.journalq.security.PasswordEncoder;
import com.jd.journalq.security.UserDetails;
import com.jd.journalq.toolkit.security.auth.AuthException;

/**
 * Created by chengzhiliang on 2018/9/27.
 */
public class TempAuthentication implements Authentication {

    public static final String USERNAME = "admin";

    public static final String PASSWORD = "admin";

    @Override
    public UserDetails getUser(String user) throws AuthException {
        if (!USERNAME.equals(user)) {
            return null;
        }
        return new TempUserDetails();
    }

    @Override
    public PasswordEncoder getPasswordEncode() {
        return new TempPasswordEncoder();
    }

    @Override
    public BooleanResponse auth(String userName, String password) {
        return null;
    }

    @Override
    public BooleanResponse auth(String userName, String password, boolean checkAdmin) {
        return null;
    }

    @Override
    public boolean isAdmin(String userName) {
        return false;
    }

    public class TempUserDetails implements UserDetails {

        @Override
        public String getUsername() {
            return USERNAME;
        }

        @Override
        public String getPassword() {
            return PASSWORD;
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
            return true;
        }
    }

    public class TempPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(String password) throws AuthException {
            return password;
        }
    }
}