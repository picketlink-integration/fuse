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

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.AuthorizationPolicy;
import org.apache.camel.spi.RouteContext;
import org.picketlink.Identity;
import org.picketlink.integration.fuse.camel.PicketLinkCamelProcessor;

import javax.inject.Inject;
import java.security.Principal;

/**
 * An implementation of {@link org.apache.camel.spi.AuthorizationPolicy} using PicketLink
 * @author Anil Saldhana
 * @since March 21, 2014
 */
public class PicketLinkCamelAuthorizationPolicy implements AuthorizationPolicy {
    @Inject
    protected Identity identity;

    /**
     * @see org.apache.camel.spi.AuthorizationPolicy#beforeWrap(org.apache.camel.spi.RouteContext, org.apache.camel.model.ProcessorDefinition)
     */
    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> processorDefinition) {
    }

    /**
     * @see org.apache.camel.spi.AuthorizationPolicy#wrap(org.apache.camel.spi.RouteContext, org.apache.camel.Processor)
     */
    @Override
    public Processor wrap(RouteContext routeContext, Processor processor) {
        return new PicketLinkAuthorizationProcessor(processor);
    }

    public class PicketLinkAuthorizationProcessor extends DelegateProcessor{
        public PicketLinkAuthorizationProcessor(Processor delegate){
            super(delegate);
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            preprocess(exchange);
            processNext(exchange);
        }
    }

    protected void preprocess(Exchange exchange) throws Exception{
        //Are we authenticated?
        Principal principal = (Principal) exchange.getIn().getHeader(PicketLinkCamelProcessor.PRINCIPAL_KEY);
        if(principal == null){
            throw new CamelAuthorizationException("Unauthenticated.",exchange);
        }
        //Now we look at the authorization
        boolean hasPermission = identity.hasPermission(exchange, "process");
        if(!hasPermission){
            throw new CamelAuthorizationException("UnAuthorized.",exchange);
        }
    }
}