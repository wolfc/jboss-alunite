/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.alunite.classloading;

import org.jboss.logging.Logger;
import sun.misc.CompoundEnumeration;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Combine multiple class loaders into one.
 *
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AluniteClassLoader extends ClassLoader
{
   private static final Logger log = Logger.getLogger(AluniteClassLoader.class);
   
   private ClassLoader[] delegates;

   public AluniteClassLoader(ClassLoader... delegates)
   {
      super(null);
      
      this.delegates = delegates;
   }

   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException
   {
      for(ClassLoader cl : delegates)
      {
         try
         {
            return cl.loadClass(name);
         }
         catch(ClassNotFoundException e)
         {
            // ignore
         }
      }
      return super.findClass(name);
   }

   @Override
   protected URL findResource(String name)
   {
      for(ClassLoader cl : delegates)
      {
         URL url = cl.getResource(name);
         if(url != null)
            return url;
      }
      return super.findResource(name);
   }

   @Override
   protected Enumeration<URL> findResources(String name) throws IOException
   {
      Enumeration tmp[] = new Enumeration[delegates.length];
      for(int i = 0; i < tmp.length; i++)
         tmp[i] = delegates[i].getResources(name);
      return new CompoundEnumeration(tmp);
   }
}
