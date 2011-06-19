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
package org.jboss.alunite.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class FilteringClassLoader extends ClassLoader {
    public static interface Filter {
        boolean acceptClass(String name);
        boolean acceptResource(String name);
    }

    private final Filter filter;

    public FilteringClassLoader(final Filter filter, final ClassLoader parent) {
        super(parent);
        this.filter = filter;
    }

    @Override
    public URL getResource(String name) {
        if (!filter.acceptResource(name))
            return null;
        return super.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (!filter.acceptResource(name))
            return null;
        return super.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (!filter.acceptResource(name))
            return null;
        return super.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!filter.acceptClass(name))
            return null;
        return super.loadClass(name);
    }

    @Override
    public String toString() {
        return FilteringClassLoader.class.getName() + "{" +
                "filter=" + filter +
                '}';
    }
}
