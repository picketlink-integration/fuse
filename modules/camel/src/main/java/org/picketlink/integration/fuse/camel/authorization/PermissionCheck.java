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
package org.picketlink.integration.fuse.camel.authorization;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;

/**
 * Represents a permission for which the currently authenticated user is tested for.
 */
public class PermissionCheck {

    private final Object resource;
    private final String operation;
    private final IdentityManager identityManager;
    private final RelationshipManager relationshipManager;
    private final IdentityType identityType;

    private boolean granted = false;

    public PermissionCheck(PartitionManager partitionManager, IdentityType identityType, Object resource, String operation) {
        this.identityType = identityType;
        this.resource = resource;
        this.operation = operation;
        this.identityManager = partitionManager.createIdentityManager(identityType.getPartition());
        this.relationshipManager = partitionManager.createRelationshipManager();
    }

    public Object getResource() {
        return resource;
    }

    public String getOperation() {
        return operation;
    }

    public void grant() {
        this.granted = true;
    }

    public boolean isGranted() {
        return granted;
    }

    public IdentityManager getIdentityManager() {
        return identityManager;
    }

    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
    }

    public boolean hasRole(String role) {
        Role storedRole = BasicModel.getRole(identityManager, role);
        if (storedRole == null) {
            throw new RuntimeException("Role not found: " + role);
        }
        return BasicModel.hasRole(relationshipManager, identityType, storedRole);
    }

    public boolean isMember(String groupName){
        Group storedGroup = BasicModel.getGroup(identityManager,groupName);
        if(storedGroup == null){
            throw new RuntimeException(groupName + " does not exist");
        }
        return BasicModel.isMember(relationshipManager,(Account)identityType,storedGroup);
    }
}