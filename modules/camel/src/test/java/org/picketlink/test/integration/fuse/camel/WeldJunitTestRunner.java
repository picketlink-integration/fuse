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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit Runner capable of handling CDI Weld startup
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class WeldJunitTestRunner extends BlockJUnit4ClassRunner {
    private final Class<?> klass;
    private final Weld weld;
    private final WeldContainer container;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws org.junit.runners.model.InitializationError if the test class is malformed.
     */
    public WeldJunitTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.klass = klass;
        this.weld = new Weld();
        this.container = weld.initialize();
    }

    @Override
    protected Object createTest() throws Exception {

        /*
         * Instance<Object> instances = container.instance();
         *
         * Iterator<Object> iterator = instances.iterator(); while(iterator.hasNext()){ Object object = iterator.next();
         *
         * if(object.getClass() == PicketLinkCamelProcessor.class && klass == PicketLinkCamelProcessor.class){ return object; }
         * }
         */
        return container.instance().select(klass).get();
    }
}
