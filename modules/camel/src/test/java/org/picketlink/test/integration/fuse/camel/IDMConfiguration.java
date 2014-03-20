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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;

/**
 * A Simple bean to create the PicketLink IDM configuration.
 * By Default, the file store will be used to store identities
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
@ApplicationScoped
public class IDMConfiguration {
    private IdentityConfiguration identityConfig = null;

    @Produces
    IdentityConfiguration createConfig() {
        if (identityConfig == null) {
            initConfig();
        }
        return identityConfig;
    }
    private void initConfig() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .file()
                .supportAllFeatures();

        identityConfig = builder.build();
    }
}
