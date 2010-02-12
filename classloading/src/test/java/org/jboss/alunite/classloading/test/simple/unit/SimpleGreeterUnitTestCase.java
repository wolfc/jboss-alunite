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
package org.jboss.alunite.classloading.test.simple.unit;

import org.jboss.alunite.classloading.AluniteClassLoader;
import org.jboss.alunite.classloading.test.simple.GreeterBean;
import org.jboss.alunite.classloading.test.simple.GreeterRemote;
import org.jboss.alunite.deployer.Deployer;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleGreeterUnitTestCase
{
   private static final Logger log = Logger.getLogger(SimpleGreeterUnitTestCase.class);

   private static URL clientUrl;
   private static Deployer deployer;
   private static URL deploymentURL;

   @AfterClass
   public static void afterClass()
   {
      if(deploymentURL != null)
         deployer.undeploy(deploymentURL);
   }

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      String jbossHome = System.getenv("JBOSS_HOME");
      if(jbossHome == null)
         jbossHome = System.getProperty("jboss.home");
      if(jbossHome == null)
         throw new IllegalStateException("Neither JBOSS_HOME (env) nor jboss.home (property) is set");

      File jbossHomeDir = new File(jbossHome);
      clientUrl = new URL(jbossHomeDir.toURI().toURL(), "client/jbossall-client.jar");
      URL urls[] = { clientUrl };
      URLClassLoader cl = new URLClassLoader(urls);
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
         Class<Deployer> cls = (Class<Deployer>) cl.loadClass(Deployer.class.getName());
         deployer = cls.newInstance();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(previous);
      }

      JavaArchive archive = createDeployment();
      File target = File.createTempFile("test", ".jar");
      target.deleteOnExit();
      archive.as(ZipExporter.class).exportZip(target, true);
      deploymentURL = target.toURI().toURL();
      deployer.redeploy(deploymentURL);
   }

   public static JavaArchive createDeployment()
   {
      return Archives.create("test.jar", JavaArchive.class)
         .addClasses(GreeterBean.class, GreeterRemote.class);
   }

   @Test
   public void testNegative() throws Exception
   {
      URL urls[] = { clientUrl };
      // have the app cl as parent, so we get a dirty environment.
      URLClassLoader cl = new URLClassLoader(urls);
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
         InitialContext ctx = new InitialContext();
         GreeterRemote bean = (GreeterRemote) ctx.lookup("Greeter");
         String result = bean.sayHi("testNegative");
         assertEquals("fake interceptor", result);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(previous);
      }
   }

   @Test
   public void testPositive() throws Exception
   {
      URL urls[] = { clientUrl };
      // don't set a parent, so we run in complete isolation.
      URLClassLoader urlCl = new URLClassLoader(urls, null);
      // since we're running in isolation my own interface needs to be added.
      ClassLoader cl = new AluniteClassLoader(urlCl, ClassLoader.getSystemClassLoader());
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
         InitialContext ctx = new InitialContext();
         GreeterRemote bean = (GreeterRemote) ctx.lookup("Greeter");
         String result = bean.sayHi("testPositive");
         assertEquals("Hi testPositive", result);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(previous);
      }
   }
}
