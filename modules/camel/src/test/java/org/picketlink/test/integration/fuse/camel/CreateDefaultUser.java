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

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.integration.fuse.camel.authorization.PicketLinkCamelDroolsVoter;

/**
 * Create some users
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
@Singleton
public class CreateDefaultUser {
    @Inject
    PartitionManager partitionManager;

    public void create() {
        IdentityManager identityManager = partitionManager.createIdentityManager();

        User admin = new User("admin");
        admin.setEmail("admin@acme.com");

        identityManager.add(admin);
        identityManager.updateCredential(admin, new Password("adminpwd"));

        // Create Sales User
        User sales = new User("sales");
        sales.setEmail("sales@acme.com");

        identityManager.add(sales);
        identityManager.updateCredential(sales, new Password("salespwd"));

        // Let create roles

        // Create role "manager"
        Role managerRole = new Role("manager");
        identityManager.add(managerRole);

        // Create application role "sales"
        Role salesRole = new Role("sales");
        identityManager.add(salesRole);

        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        BasicModel.grantRole(relationshipManager, admin, managerRole);
        BasicModel.grantRole(relationshipManager, sales, salesRole);
    }

    public void printHello(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        create();
        System.out.println("Default User created");
    }
}