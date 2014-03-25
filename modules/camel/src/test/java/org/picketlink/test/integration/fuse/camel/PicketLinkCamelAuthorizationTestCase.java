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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.integration.fuse.camel.authorization.PicketLinkCamelAuthorizationPolicy;

/**
 * Unit test the {@link org.picketlink.integration.fuse.camel.authorization.PicketLinkCamelAuthorizationPolicy}
 *
 * @author Anil Saldhana
 * @since March 21, 2014
 */
@RunWith(WeldJunitTestRunner.class)
public class PicketLinkCamelAuthorizationTestCase extends PicketLinkCamelBaseTestCase {
    @Inject
    protected PicketLinkCamelAuthorizationPolicy picketLinkCamelAuthorizationPolicy;

    @Test
    public void sendGroceryOrder() throws Exception {
        mockGrocery.expectedBodiesReceived("grocery order");
        mockElectronics.expectedMessageCount(0);
        mockOther.expectedMessageCount(0);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("FileName", "grocery.txt");
        headers.put("username", "admin");
        headers.put("password", "adminpwd");

        producerTemplate.sendBodyAndHeaders("grocery order", headers);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void sendGroceryAndElectronicsOrder() throws Exception {
        mockGrocery.expectedBodiesReceived("grocery order");
        mockElectronics.expectedBodiesReceived("electronics order");
        mockOther.expectedMessageCount(0);

        Map<String, Object> groceryHeaders = new HashMap<String, Object>();
        groceryHeaders.put("FileName", "grocery.txt");
        groceryHeaders.put("username", "admin");
        groceryHeaders.put("password", "adminpwd");

        Map<String, Object> electronicsHeaders = new HashMap<String, Object>();
        electronicsHeaders.put("FileName", "electronics.txt");
        electronicsHeaders.put("username", "admin");
        electronicsHeaders.put("password", "adminpwd");

        producerTemplate.sendBodyAndHeaders("grocery order", groceryHeaders);
        producerTemplate.sendBodyAndHeaders("electronics order", electronicsHeaders);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void sendsInvalidOrder() throws Exception {
        mockGrocery.expectedMessageCount(0);
        mockElectronics.expectedMessageCount(0);
        mockOther.expectedMessageCount(0);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("FileName", "invalidorder.txt");
        headers.put("username", "admin");
        headers.put("password", "adminpwd");

        try {
            producerTemplate.sendBodyAndHeaders("Test message", headers);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof CamelAuthorizationException);
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                assertNotNull(picketLinkCamelProcessor);

                from("file://source").process(picketLinkCamelProcessor).policy(picketLinkCamelAuthorizationPolicy).choice()
                        .when(simple("${in.header.FileName} contains 'grocery.txt'")).to("file://grocery")
                        .when(simple("${in.header.FileName} contains 'electronics.txt'")).to("file://electronics").otherwise()
                        .to("log://lowpriority");
            }
        };
    }
}
