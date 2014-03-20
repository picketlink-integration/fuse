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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.context.unbound.RequestContextImpl;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * An ugly hacked up {@link javax.enterprise.inject.spi.Extension} for Weld so
 * that RequestScoped and SessionScoped are active in the JUnit/JavaSE environment.
 *
 * Remember, weld-se does not support either the request scope or the session scope.
 *
 * This is used for JUnit testing only. So no harm done.
 *
 * This extension is loaded via the JDK Service Loader Mechanism and look for a
 * file in META-INF/services directory called javax.enterprise.inject.spi.Extension
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class WeldServletScopesSupportForSe implements Extension {
    public void afterDeployment(@Observes AfterDeploymentValidation event, BeanManager beanManager) {

        try {
            setContextActive(beanManager, SessionScoped.class);
            setContextActive(beanManager, RequestScoped.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContextActive(BeanManager beanManager, Class<? extends Annotation> cls) throws Exception {
        BeanManagerImpl beanManagerImpl = null;
        if (beanManager instanceof BeanManagerProxy) {
            BeanManagerProxy proxy = (BeanManagerProxy) beanManager;
            beanManagerImpl = proxy.delegate();
        } else {
            beanManagerImpl = (BeanManagerImpl) beanManager;
        }
        //Ugly hack to make the "contexts" map inside the BeanManagerImpl accessible
        Field f = beanManagerImpl.getClass().getDeclaredField("contexts"); // NoSuchFieldException
        f.setAccessible(true);
        Map<Class<? extends Annotation>, List<Context>> contexts = (Map<Class<? extends Annotation>, List<Context>>) f
                .get(beanManagerImpl);

        List<Context> registeredContexts = contexts.get(cls);
        RequestContextImpl context = new RequestContextImpl("dummy");
        context.activate();

        List<Context> newList = new ArrayList<Context>();
        newList.add(context);

        contexts.put(cls, newList);

        boolean active = beanManagerImpl.isContextActive(cls);
        if (!active) {
            throw new Exception(cls.getName() + " scope is not active");
        }
    }
}
