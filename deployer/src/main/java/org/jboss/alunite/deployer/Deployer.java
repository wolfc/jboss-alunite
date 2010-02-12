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
package org.jboss.alunite.deployer;

import javax.management.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Deployer
{
   private MBeanServerConnection server;
   private ObjectName name;

   public Deployer()
   {
      try
      {
         name = new ObjectName("jboss.system:service=MainDeployer");
         InitialContext ctx = new InitialContext();
         server = (MBeanServerConnection) ctx.lookup("jmx/invoker/RMIAdaptor");
      }
      catch(MalformedObjectNameException e)
      {
         throw new RuntimeException(e);
      }
      catch(NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   protected Object invoke(String operationName, Object params[], String signature[])
   {
      try
      {
         return server.invoke(name, operationName, params, signature);
      }
      catch(InstanceNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      catch(ReflectionException e)
      {
         throw new RuntimeException(e);
      }
      catch(MBeanException e)
      {
         throw new RuntimeException(e);
      }
      catch(IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void redeploy(URL url)
   {
      Object params[] = { url };
      String signature[] = { URL.class.getName() };
      invoke("redeploy", params, signature);
   }

   public void undeploy(URL url)
   {
      Object params[] = { url };
      String signature[] = { URL.class.getName() };
      invoke("undeploy", params, signature);
   }
}
