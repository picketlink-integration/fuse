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

import java.io.Serializable;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.spi.PermissionVoter;

/**
 * An implementation of {@link org.picketlink.idm.permission.spi.PermissionVoter} based on Drools
 *
 * To be replaced by picketlink-idm-drools
 *
 * @author Anil Saldhana
 * @since March 24, 2014
 */
public class PicketLinkCamelDroolsVoter implements PermissionVoter {
    private KieBase securityRuleBase;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private IdentityManager identityManager;

    public PicketLinkCamelDroolsVoter(KieBase ruleSet) {
        this.securityRuleBase = ruleSet;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    @Override
    public VotingResult hasPermission(IdentityType recipient, Object resource, String operation) {
        Exchange camelExchange = (Exchange) resource;
        Message camelMessage = camelExchange.getIn();

        VotingResult result = VotingResult.NOT_APPLICABLE;

        KieSession session = securityRuleBase.newKieSession();

        PermissionCheck check = new PermissionCheck(partitionManager, recipient, resource, operation);

        IdentityManager idm = partitionManager.createIdentityManager();
        RelationshipManager relation = partitionManager.createRelationshipManager();

        session.insert(idm);
        session.insert(relation);
        
        session.insert(camelMessage);
        session.insert(partitionManager.createIdentityManager());

        session.insert(recipient);
        session.insert(check);
        session.fireAllRules();

        if (check.isGranted()) {
            result = VotingResult.ALLOW;
        }

        return result;
    }

    @Override
    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        return VotingResult.NOT_APPLICABLE;
    }
}
