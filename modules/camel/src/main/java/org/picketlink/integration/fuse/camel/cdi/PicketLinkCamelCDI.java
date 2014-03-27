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
package org.picketlink.integration.fuse.camel.cdi;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.permission.spi.PermissionVoter;
import org.picketlink.integration.fuse.camel.authorization.PicketLinkCamelDroolsVoter;

/**
 * Class that deals with {@link javax.enterprise.inject.Produces}
 *
 * @author Anil Saldhana
 * @since March 24, 2014
 */
public class PicketLinkCamelCDI {

    protected final static String SECURITY_RULES = "camel-security.drl";

    protected PicketLinkCamelDroolsVoter picketLinkCamelDroolsVoter;

    protected KieContainer kieContainer;

    protected KieSession kSession;

    @Inject
    private PartitionManager partitionManager;

    @Produces
    public PermissionVoter createPermissionVoter() {
        if (picketLinkCamelDroolsVoter == null) {
            picketLinkCamelDroolsVoter = new PicketLinkCamelDroolsVoter(getKieBase());
            picketLinkCamelDroolsVoter.setPartitionManager(partitionManager);
        }
        return picketLinkCamelDroolsVoter;
    }

    @Produces
    public KieSession createKieSession() {
        if (kSession == null) {
            kieContainer = getKieContainer();
            return kieContainer.newKieSession("ksession1");
        }
        return kSession;
    }

    protected KieBase getKieBase() {
        KieContainer kieContainer = getKieContainer();
        KieServices kServices = KieServices.Factory.get();

        KieBaseConfiguration kieBaseConfiguration = kServices.newKieBaseConfiguration();
        return kieContainer.newKieBase(kieBaseConfiguration);
    }

    protected KieContainer getKieContainer() {
        if (kieContainer == null) {
            KieServices ks = KieServices.Factory.get();

            kieContainer = ks.getKieClasspathContainer();
            /*
             * KieSession kSession = kieContainer.newKieSession("ksession2");
             *
             * KieRepository kr = ks.getRepository();
             *
             * InputStream in = getClass().getClassLoader().getResourceAsStream(SECURITY_RULES);
             *
             * if(in == null){ throw new RuntimeException("camel-security.drl not located"); } KieResources kieResources =
             * ks.getResources(); Resource resource = kieResources.newInputStreamResource(in); if(resource == null){ throw new
             * RuntimeException("Kie Resource not found"); }
             *
             * KieModule kModule = kr.addKieModule(resource);
             *
             * kieContainer = ks.newKieContainer(kModule.getReleaseId());
             */
        }
        return kieContainer;
    }
}
