package com.itc.employeeleaveattendance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class AppRunner {

    public static void main(String[] args) throws Exception {

        String webappDirLocation = "src/main/webapp/";

        System.out.println("1. Creating Tomcat...");
        Tomcat tomcat = new Tomcat();

        System.out.println("2. Setting port 8080...");
        tomcat.setPort(8080);
        tomcat.getConnector();

        System.out.println("3. Adding web application...");

        StandardContext ctx = (StandardContext) tomcat.addWebapp(
                "/EmployeeCaseStudyTracker",
                new File(webappDirLocation).getAbsolutePath());

        System.out.println("4. Web application added.");

        // Session timeout: 30 minutes
        ctx.setSessionTimeout(30);

        // Use Maven exec classloader so the webapp can access
        // Maven-managed dependencies such as JSP/Tomcat libraries.
        ctx.setParentClassLoader(
                Thread.currentThread().getContextClassLoader());
        ctx.setDelegate(true);

        System.out.println("5. Configuring web resources...");

        // Expose compiled classes under /WEB-INF/classes
        WebResourceRoot resources = new StandardRoot(ctx);

        resources.addPreResources(
                new DirResourceSet(
                        resources,
                        "/WEB-INF/classes",
                        new File("target/classes").getAbsolutePath(),
                        "/"));

        ctx.setResources(resources);

        System.out.println("6. Web resources configured.");

        System.out.println("7. Starting Tomcat...");

        tomcat.start();

        System.out.println("8. Tomcat started successfully!");
        System.out.println("9. Application URL:");
        System.out.println(
                "   http://localhost:8080/EmployeeCaseStudyTracker");

        System.out.println("10. Waiting for requests...");

        tomcat.getServer().await();
    }
}