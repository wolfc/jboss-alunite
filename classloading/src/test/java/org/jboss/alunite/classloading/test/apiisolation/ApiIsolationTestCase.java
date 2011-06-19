/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2011, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.alunite.classloading.test.apiisolation;

import org.jboss.alunite.classloading.test.apiisolation.api.Value;
import org.jboss.alunite.classloading.test.apiisolation.impl.SecretValueImpl;
import org.jboss.alunite.classloading.test.apiisolation.user.ValueProcessor;
import org.jboss.alunite.common.FilteringClassLoader;
import org.jboss.alunite.common.RedefiningClassLoader;
import org.jboss.alunite.common.TargetExceptionHandler;
import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.Subject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.security.PrivilegedExceptionAction;

import static org.jboss.alunite.common.Filters.filter;
import static org.jboss.alunite.common.Filters.or;
import static org.jboss.alunite.common.TargetExceptionHandler.handle;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class ApiIsolationTestCase {
    @Ignore("should not work")
    @Test
    public void testNoIsolation() {
        final SecretValueImpl impl = new SecretValueImpl("testNoIsolation");
        final ValueProcessor processor = new ValueProcessor();
        final String result = processor.process(impl);
        assertEquals("Processed testNoIsolation", result);
    }

    @Test
    public void testIsolation() throws Exception {
        assertEquals(SecretValueImpl.class.getName(), ValueProcessor.SECRET);
        final Subject subject = new Subject();
        subject.getPrincipals().add(new TestPrincipal("tester"));
        subject.setReadOnly();
        final URLClassLoader tccl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        final ClassLoader apiClassLoader = Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<ClassLoader>() {
            public ClassLoader run() throws Exception {
                return new FilteringClassLoader(or(filter(TargetExceptionHandler.class, Value.class), filter("java", "javax", "org.junit")), new RedefiningClassLoader(tccl, Value.class));
            }
        }, null);
        final Object secretValue = Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                final URLClassLoader implClassLoader = new RedefiningClassLoader(apiClassLoader, tccl.getURLs(), SecretValueImpl.class);
                final Class<?> secretValueClass = Class.forName(SecretValueImpl.class.getName(), true, implClassLoader);
                return secretValueClass.getConstructor(String.class).newInstance("testIsolation");
            }
        }, null);
        final Object processor = Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                final URLClassLoader userClassLoader = new RedefiningClassLoader(apiClassLoader, tccl.getURLs(), ValueProcessor.class);
                final Class<?> processorClass = Class.forName(ValueProcessor.class.getName(), true, userClassLoader);
                return processorClass.newInstance();
            }
        }, null);
        final Method process = Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
                final Class<?> valueClass = Class.forName(Value.class.getName(), true, apiClassLoader);
                return processor.getClass().getMethod("process", valueClass);
            }
        }, null);
        //Thread.currentThread().setContextClassLoader(userClassLoader);
        try {
            Object result = process.invoke(processor, secretValue);
            assertEquals("Processed testIsolation", result);
        } catch (final InvocationTargetException e) {
            throw handle(e);
        } finally {
            //Thread.currentThread().setContextClassLoader(tccl);
        }
    }
}
