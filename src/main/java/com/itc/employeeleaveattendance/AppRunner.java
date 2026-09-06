package com.itc.employeeleaveattendance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Embedded Tomcat runner for development.
 * Starts the application on http://localhost:8080/EmployeeCaseStudyTracker
 */
public class AppRunner {

    public static void main(String[] args) throws Exception {
        String webappDirLocation = "src/main/webapp/";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        StandardContext ctx = (StandardContext) tomcat.addWebapp(
                "/EmployeeCaseStudyTracker",
                new File(webappDirLocation).getAbsolutePath()
        );

        // Session timeout: 30 minutes
        ctx.setSessionTimeout(30);

        // CRITICAL for embedded mode with exec:java:
        // Delegate class loading to the parent (Maven exec) classloader first.
        // This lets the webapp see JspServlet (tomcat-embed-jasper) and all
        // Maven-managed JARs that are on the exec plugin's classpath.
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        ctx.setDelegate(true);

        // Expose compiled classes into /WEB-INF/classes
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(
                resources,
                "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(),
                "/"
        ));
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();
    }
}
