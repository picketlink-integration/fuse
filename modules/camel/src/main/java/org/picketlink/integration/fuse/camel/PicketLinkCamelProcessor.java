/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.integration.fuse.camel;

import java.security.Principal;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

/**
 * Implementation of {@link org.apache.camel.Processor} using PicketLink
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class PicketLinkCamelProcessor implements Processor {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String PRINCIPAL_KEY = "principal";

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credential;

    @Override
    public void process(Exchange exchange) throws Exception {
        if (identity == null) {
            throw new LoginException("identity is not injected");
        }
        if (credential == null) {
            throw new LoginException("credential is not injected");
        }

        Message incomingMessage = exchange.getIn();

        String username = incomingMessage.getHeader(USERNAME_KEY, String.class);
        String password = incomingMessage.getHeader(PASSWORD_KEY, String.class);

        boolean shouldWeLogOut = preLogOut(username);
        if (shouldWeLogOut) {
            // we got a new user on the message
            identity.logout();
        }

        if (identity.isLoggedIn() == false) {
            if (username == null) {
                throw new LoginException("Username is null");
            }
            if (password == null || password.isEmpty()) {
                throw new LoginException("password is null or empty");
            }
            // Try to authenticate using PicketLink
            credential.setUserId(username);
            credential.setCredential(new Password(password));

            identity.login();
        }

        if (identity.isLoggedIn()) {
            incomingMessage.removeHeader(USERNAME_KEY);
            incomingMessage.removeHeader(PASSWORD_KEY);
            final String currentUser = getCurrentUser();
            incomingMessage.setHeader(PRINCIPAL_KEY, new Principal() {
                @Override
                public String getName() {
                    return currentUser;
                }
            });
        } else {
            // Unsuccessful
            throw new LoginException("Authentication failed");
        }
    }

    /**
     * {@link org.picketlink.Identity} is session scoped. If the message came with an username that is different from what we
     * are currently logged in, we would like to reauthenticate the user
     *
     * @param passedUserName
     * @return
     */
    protected boolean preLogOut(String passedUserName) {
        if (identity.isLoggedIn()) {
            String currentUser = getCurrentUser();
            if (currentUser.equals(passedUserName) == false) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current authenticated user
     *
     * @return
     */
    protected String getCurrentUser() {
        String loginName = null;

        if (identity.isLoggedIn()) {
            Account account = identity.getAccount();
            if (account instanceof User) {
                User user = (User) account;
                loginName = user.getLoginName();
            } else {
                loginName = account.getId();
            }
        }
        return loginName;
    }
}
