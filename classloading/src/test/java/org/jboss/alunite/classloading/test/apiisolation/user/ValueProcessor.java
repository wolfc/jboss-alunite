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
package org.jboss.alunite.classloading.test.apiisolation.user;

import org.jboss.alunite.classloading.test.apiisolation.api.Value;
import org.jboss.alunite.classloading.test.apiisolation.impl.SecretValueImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;

import static org.jboss.alunite.common.TargetExceptionHandler.handle;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class ValueProcessor {
    public static final String SECRET = "org.jboss.alunite.classloading.test.apiisolation.impl.SecretValueImpl";

    private static String classLoaderId(final Class<?> cls) {
        try {
            return cls.getClassLoader().toString();
        } catch (AccessControlException e) {
            return "unknown (" + e.getMessage() + ")";
        }
    }

    public String process(final Value value) {
        try {
            ((SecretValueImpl) value).publicKey();
            fail("Expected NoClassDefFoundError");
//        } catch (ClassCastException e) {
//            // darn, can't hack it that way
        } catch (NoClassDefFoundError e) {
            // the best way
        }
        // this is purely for full implementation hiding
        try {
            Class<?> cls = Class.forName(SECRET);
            fail("Expected ClassNotFoundException, found SecretValueImpl on " + classLoaderId(cls));
        } catch (ClassNotFoundException e) {
            // good
        }
        /* picking it up from another class loader will just result in an IllegalArgumentException: object is not an instance of declaring class
        try {
            final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(SecretValueImpl.class.getName());
            final Method secret = cls.getMethod("secret");
            secret.invoke(value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        */
        // I've asserted I can't find the class in any way. Can I still call a package private method on it?
        try {
            final Method secret = value.getClass().getDeclaredMethod("secret");
            secret.setAccessible(true);
            secret.invoke(value);
        } catch (InvocationTargetException e) {
            throw handle(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return "Processed " + value.get();
    }
}
