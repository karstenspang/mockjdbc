package io.github.karstenspang.mockjdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Driver} that accepts URL's starting with {@code jdbc:mock:}.
 * The rest of the URL will be prepended by {@code jdbc:} and a connection
 * will be opened by {@link DriverManager} with the resulting URL,
 * unless intercepted.
 */
public class MockDriver implements Driver {
    private static final String pomPropertiesFile="META-INF/maven/io.github.karstenspang/mockjdbc/pom.properties";
    private static final Logger logger=Logger.getLogger(MockDriver.class.getName());
    private static final MockDriver instance;
    private static final Iterator<Step<Connection>> emptySteps;
    private Iterator<Step<Connection>> steps;
    private final int majorVersion;
    private final int minorVersion;
    static{
        instance=new MockDriver();
        try{
            DriverManager.registerDriver(instance);
        }
        // Not expected to happen
        catch(SQLException e){
            throw new RuntimeException(e);
        }
        emptySteps=new Program<Connection>(Collections.emptyList()).iterator();
    }
    
    /**
     * Set the program of the driver. Initially, the program
     * is one that always returns {@link PassThruStep}, in
     * other words, real {@link Connection}s are returned.
     * Since the program is static, test cases involving
     * {@link MockDriver} can not be run in parallel.
     * @param program Program to use. If {@code null},
     *        the initial program is reinstated.
     */
    public static void setProgram(Program<Connection> program){
        logger.fine("Setting program "+String.valueOf(program));
        instance.steps=program==null?emptySteps:program.iterator();
    }
    
    /**
     * Takes an URL of the form {@code jdbc:mock:restofurl}, and
     * converts it into {@code jdbc:restofurl}, passing that to
     * {@link DriverManager#getConnection(String,Properties)}.
     * This can be intercepted by a {@link Program}.
     * @param url The JDBC mock URL.
     * @param info Additional info to be passed to the real driver.
     * @return Connection to the real driver, {@code null} if {@code url}
     *         is does not start with {@code jdbc:mock:}.
     * @throws SQLException if the program dictates it, or connecting
     *         using the real driver fails.
     * @throws IllegalArgumentException if {@code url} starts with
     *         {@code "jdbc:mock:mock:"}.
     */
    @Override
    public Connection connect​(final String url,final Properties info)
        throws SQLException
    {
        logger.fine("connect("+String.valueOf(url)+","+String.valueOf(info)+")");
        if (!isOurUrl(url)) return null;
        final String newUrl="jdbc:"+url.split(":",3)[2];
        if (isOurUrl(newUrl)) throw new IllegalArgumentException("Self referencing URL: "+url);
        Step<Connection> step=steps.next();
        logger.fine("Applying "+String.valueOf(step)+" to DriverManager.getConnection("+String.valueOf(newUrl)+","+String.valueOf(info)+")");
        return step.apply(()->DriverManager.getConnection(newUrl,info));
    }
    @Override
    public boolean acceptsURL​(String url){return isOurUrl(url);}
    @Override
    public int getMajorVersion(){return majorVersion;}
    @Override
    public int getMinorVersion(){return minorVersion;}
    @Override
    public Logger getParentLogger(){return Logger.getLogger(MockDriver.class.getPackage().getName());}
    @Override
    public DriverPropertyInfo[] getPropertyInfo​(String url, Properties info){return new DriverPropertyInfo[0];}
    @Override
    public boolean jdbcCompliant(){return true;}
    
    private MockDriver()
    {
        this(pomPropertiesFile);
    }
    
    // For testing methods other than connect
    MockDriver(String propertyFile)
    {
        steps=emptySteps;
        Properties pomProperties;
        try {
            pomProperties=loadProperties(propertyFile);
        }
        catch(IOException e){
            logger.warning("Could not load property file "+String.valueOf(propertyFile));
            pomProperties=new Properties();
        }
        
        logger.info("MockDriver version "+String.valueOf(pomProperties.getProperty("version")));
        int[] versionParts=extractVersion(pomProperties.getProperty("version"));
        majorVersion=versionParts.length>=1?versionParts[0]:0;
        minorVersion=versionParts.length>=2?versionParts[1]:0;
    }
    
    private static boolean isOurUrl(String url){
        return url.startsWith("jdbc:mock:");
    }
    
    private static Properties loadProperties(String fileName)
        throws IOException
    {
        Properties props=new Properties();
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        // Never null with OpenJDK
        if (loader==null) loader=ClassLoader.getSystemClassLoader();
        InputStream stream=loader.getResourceAsStream(fileName);
        if (stream==null) throw new IOException("Could not open resource "+fileName);
        try (Reader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)){
            props.load(reader);
        }
        return props;
    }
    
    private static int[] extractVersion(String versionString)
    {
        if (versionString==null) return new int[0];
        Pattern p=Pattern.compile("(\\d+(\\.\\d+)*).*");
        Matcher m=p.matcher(versionString);
        if (!m.matches()) return new int[0];
        String versionPrefix=m.group(1);
        String[] versionParts=versionPrefix.split("\\.");
        int[] result=new int[versionParts.length];
        for (int i=0;i<versionParts.length;i++){
            result[i]=Integer.parseInt(versionParts[i]);
        }
        return result;
    }
}
