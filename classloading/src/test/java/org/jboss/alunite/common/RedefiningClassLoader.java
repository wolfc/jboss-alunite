/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.alunite.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * A sneaky class loader that takes all the credit for loading a class. :-)
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class RedefiningClassLoader extends URLClassLoader {
    private ClassLoader master;
    private Set<String> classNames = new HashSet<String>();

    public RedefiningClassLoader(URLClassLoader master, Class<?>... classes) {
        super(master.getURLs(), null);

        this.master = master;
        for (Class<?> cls : classes)
            classNames.add(cls.getName());
    }

    public RedefiningClassLoader(ClassLoader master, URL[] urls, Class<?>... classes) {
        super(urls, null);

        this.master = master;
        for (Class<?> cls : classes)
            classNames.add(cls.getName());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classNames.contains(name))
            return super.findClass(name);
        return master.loadClass(name);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            if (classNames.contains(name))
                c = findClass(name);
            else
                return super.loadClass(name, resolve);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}