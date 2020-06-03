package org.exodus.backup.bootstrap;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.servlet.Listener;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Run the main method and that's it. For how Weld recommends starting
 * applications outside a JEE container, see {@link StartMain}.
 *
 * @see StartMain
 */
public class Main {

    public static String[] PARAMETERS;

    public static String[] getParameters() {
        final String[] copy = new String[Main.PARAMETERS.length];
        System.arraycopy(Main.PARAMETERS, 0, copy, 0, Main.PARAMETERS.length);
        return copy;
    }

    /**
     * The main method called from the command line.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
        try {
            new Main(args).startWeld();
        } catch (ServletException | LifecycleException | IOException e) {
            e.printStackTrace();
        }
    }


    static int getListeningPort() {
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }
        return Integer.parseInt(webPort);
    }


    public Main(final String[] commandLineArgs) throws ServletException, LifecycleException, IOException {

        String hostName = "localhost";
        String contextPath = "";
        String tomcatBaseDir = TomcatUtil.createTempDir("tomcat",

                getListeningPort()).getAbsolutePath();
        String contextDocBase = TomcatUtil.createTempDir("tomcat-docBase",
                getListeningPort()).getAbsolutePath();


        Main.PARAMETERS = commandLineArgs;

        final Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatBaseDir);

        tomcat.setPort(getListeningPort());
        tomcat.setHostname(hostName);

        Host host = tomcat.getHost();
        Context context = tomcat.addWebapp(host, contextPath, contextDocBase, new EmbeddedContextConfig());

        context.setJarScanner(new EmbededStandardJarScanner());

        ClassLoader classLoader = Main.class.getClassLoader();
        context.setParentClassLoader(classLoader);

        // context load WEB-INF/web.xml from classpath
        context.addLifecycleListener(new WebXmlMountListener());

        context.addApplicationListener(Listener.class.getName());

        // Enable JNDI InitialContext
        tomcat.enableNaming();

        // Start server
        tomcat.start();
        tomcat.getServer().await();

    }

    public WeldContainer startWeld() {
        final Weld weld = new Weld();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(weld));
        return weld.initialize();
    }

    static class ShutdownHook extends Thread {
        private final Weld weld;

        ShutdownHook(final Weld weld) {
            this.weld = weld;
        }

        @Override
        public void run() {
            this.weld.shutdown();
        }
    }
}