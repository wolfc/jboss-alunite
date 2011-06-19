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

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Filters {
    public static FilteringClassLoader.Filter filter(final Class<?>... classes) {
        final Set<String> classSet = new HashSet<String>();
        for (final Class<?> cls : classes)
            classSet.add(cls.getName());
        return new FilteringClassLoader.Filter() {
            public boolean acceptClass(String name) {
                return classSet.contains(name);
            }

            public boolean acceptResource(String name) {
                throw new RuntimeException("NYI: .acceptResource");
            }

            public String toString() {
                return "Filter classes " + classSet;
            }
        };
    }

    public static FilteringClassLoader.Filter filter(final String... packagePrefixes) {
        return new FilteringClassLoader.Filter() {
            public boolean acceptClass(String name) {
                for (final String prefix : packagePrefixes) {
                    if (name.startsWith(prefix)) {
                        return true;
                    }
                }
                return false;
            }

            public boolean acceptResource(String name) {
                throw new RuntimeException("NYI: .acceptResource");
            }
        };
    }

    public static FilteringClassLoader.Filter or(final FilteringClassLoader.Filter... filters) {
        return new FilteringClassLoader.Filter() {
            public boolean acceptClass(String name) {
                for (final FilteringClassLoader.Filter filter : filters) {
                    if (filter.acceptClass(name))
                        return true;
                }
                return false;
            }

            public boolean acceptResource(String name) {
                for (final FilteringClassLoader.Filter filter : filters) {
                    if (filter.acceptResource(name))
                        return true;
                }
                return false;
            }

            public String toString() {
                final StringBuilder sb = new StringBuilder("Filter{");
                for (int i = 0; i < filters.length; i++) {
                    if (i > 0) sb.append(" || ");
                    sb.append(filters[i]);
                }
                sb.append("}");
                return sb.toString();
            }
        };
    }
}
