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

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit Test the {@link org.picketlink.integration.fuse.camel.PicketLinkCamelProcessor}
 * for authentication use cases
 * @author Anil Saldhana
 * @since March 25, 2014
 */
@RunWith(WeldJunitTestRunner.class)
public class PicketLinkCamelAuthenticationTestCase extends PicketLinkCamelBaseTestCase {

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
        mockOther.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("FileName", "invalidorder.txt");
        headers.put("username", "admin");
        headers.put("password", "adminpwd");

        producerTemplate.sendBodyAndHeaders("Test message", headers);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void sendGroceryAndElectronicsOrderWithInvalidUser() throws Exception {
        mockGrocery.expectedMessageCount(0);
        mockElectronics.expectedMessageCount(0);
        mockOther.expectedMessageCount(0);

        Map<String, Object> groceryHeaders = new HashMap<String, Object>();
        groceryHeaders.put("FileName", "grocery.txt");
        groceryHeaders.put("username", "BADUSER");
        groceryHeaders.put("password", "LOUSY");

        Map<String, Object> electronicsHeaders = new HashMap<String, Object>();
        electronicsHeaders.put("FileName", "electronics.txt");
        electronicsHeaders.put("username", "BADUSER");
        electronicsHeaders.put("password", "adminpwd");

        try {
            producerTemplate.sendBodyAndHeaders("grocery order", groceryHeaders);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof LoginException);
        }
        try{
            producerTemplate.sendBodyAndHeaders("electronics order", electronicsHeaders);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof LoginException);
        }
        assertMockEndpointsSatisfied();
    }

    // Private/Protected Methods
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                assertNotNull(picketLinkCamelProcessor);

                from("file://source").process(picketLinkCamelProcessor).choice()
                        .when(simple("${in.header.FileName} contains 'grocery.txt'")).to("file://grocery")
                        .when(simple("${in.header.FileName} contains 'electronics.txt'")).to("file://electronics").otherwise()
                        .to("log://lowpriority");
            }
        };
    }
}
