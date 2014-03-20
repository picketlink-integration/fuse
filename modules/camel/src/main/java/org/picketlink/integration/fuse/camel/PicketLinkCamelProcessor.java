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

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;

/**
 * Implementation of {@link org.apache.camel.Processor} using PicketLink
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class PicketLinkCamelProcessor implements Processor{
    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credential;

    @Override
    public void process(Exchange exchange) throws Exception {
        if(identity == null){
            throw new LoginException("identity is not injected");
        }
        if(credential == null){
            throw new LoginException("credential is not injected");
        }

        if(identity.isLoggedIn() == false){
            Message incomingMessage = exchange.getIn();

            String username = incomingMessage.getHeader("username", String.class);
            String password = incomingMessage.getHeader("password", String.class);


            if(username == null){
                throw new LoginException("Username is null");
            }
            if(password == null || password.isEmpty()){
                throw new LoginException("password is null or empty");
            }
            //Try to authenticate using PicketLink
            credential.setUserId(username);
            credential.setCredential(new Password(password));

            identity.login();

            if(identity.isLoggedIn()){

            }else {
                //Unsuccessful
                throw new LoginException("Authentication failed");
            }
        }
    }
}
