package com.github.karstenspang.mockjdbc;

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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Driver} that accepts URL's starting with {@code jdbc:mock:}.
 * The rest of the URL will be prepended by {@code jdbc:} and a connection
 * will be opened by {@link DriverManager} with the resulting URL.
 */
public class MockDriver implements Driver {
    private static final String pomPropertiesFile="META-INF/maven/com.github.karstenspang/mockjdbc/pom.properites";
    private static final Logger logger=Logger.getLogger(MockDriver.class.getName());
    private static final MockDriver instance;
    private static final Iterator<Step<Connection>> emptySteps;
    private Iterator<Step<Connection>> steps;
    private final int majorVersion;
    private final int minorVersion;
    static{
        try{
            instance=new MockDriver();
            DriverManager.registerDriver(instance);
        }
        catch(SQLException|IOException e){
            throw new RuntimeException(e);
        }
        emptySteps=new Program<Connection>(List.of()).iterator();
    }
    @Override
    public Connection connect​(final String url,final Properties info)
        throws SQLException
    {
        if (!url.startsWith("jdbc:mock:")) return null;
        final String newUrl="jdbc:"+url.split(":",2)[2];
        return steps.next().apply(()->DriverManager.getConnection(newUrl,info));
    }
    @Override
    public boolean acceptsURL​(String url){return url.startsWith("jdbc:mock:");}
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
        throws IOException
    {
        steps=emptySteps;
        Properties pomProperties=loadProperties(pomPropertiesFile);
        int[] versionParts=extractVersion(pomProperties.getProperty("version"));
        majorVersion=versionParts.length>=1?versionParts[0]:0;
        minorVersion=versionParts.length>=2?versionParts[1]:0;
    }
    
    /**
     * Set the program of the driver. Initially, the program
     * is one that always returns {@link PassThruStep}.
     * @param program Program to use. If {@code null},
     *        the initial program is reinstated.
     */
    public static void setProgram(Program<Connection> program){
        instance.steps=program==null?emptySteps:program.iterator();
    }
    
    private static Properties loadProperties(String fileName)
        throws IOException
    {
        Properties props=new Properties();
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
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
