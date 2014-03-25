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

import javax.inject.Inject;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.picketlink.integration.fuse.camel.PicketLinkCamelProcessor;

/**
 * Unit test the {@link org.picketlink.integration.fuse.camel.PicketLinkCamelProcessor}
 *
 * Example adapted from the ASLv2 licensed:
 * https://github.com/bibryam/camel-message-routing-examples/blob/master/routing-different-destinations
 *
 * @see org.picketlink.test.integration.fuse.camel.PicketLinkCamelAuthenticationTestCase
 * @see org.picketlink.test.integration.fuse.camel.PicketLinkCamelAuthorizationTestCase
 *
 * This base class was introduced to get over the Weld AmbiguousResolutionException.
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class PicketLinkCamelBaseTestCase extends CamelTestSupport {
    @Produce(uri = "direct:start")
    protected ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:file:grocery")
    protected MockEndpoint mockGrocery;

    @EndpointInject(uri = "mock:file:electronics")
    protected MockEndpoint mockElectronics;

    @EndpointInject(uri = "mock:log:lowpriority")
    protected MockEndpoint mockOther;

    @Inject
    protected PicketLinkCamelProcessor picketLinkCamelProcessor;

    @Before
    public void setup() throws Exception {
        super.setUp();
        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
                mockEndpointsAndSkip("*");
            }
        });
    }

}
