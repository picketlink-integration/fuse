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
package org.picketlink.test.integration.fuse.camel;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author Anil Saldhana
 * @since March 20, 2014
 */
@Singleton
public class CreateDefaultUser {
    @Inject
    PartitionManager partitionManager;

    public void create() {
        User admin = new User("admin");
        admin.setEmail("admin@acme.com");

        IdentityManager identityManager = partitionManager.createIdentityManager();

        identityManager.add(admin);
        identityManager.updateCredential(admin, new Password("adminpwd"));
    }
    public void printHello(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        create();
        System.out.println("Default User created");
    }
}
