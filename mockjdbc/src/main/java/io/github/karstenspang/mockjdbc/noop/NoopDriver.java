package io.github.karstenspang.mockjdbc.noop;

import io.github.karstenspang.mockjdbc.MockDriver;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A no-op JDBC {@link Driver} meant for mocking.
 * The driver accepts URL's starting with {@code jdbc:noop:}.
 */
public class NoopDriver implements Driver {
    private static final Logger logger=Logger.getLogger(NoopDriver.class.getName());
    private static final NoopDriver instance;
    static{
        instance=new NoopDriver();
        try{
            DriverManager.registerDriver(instance);
        }
        // Not expected to happen
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    
    /**
     * For URLs of the form {@code jdbc:noop[:xyz]},
     * returns the {@link NoopConnection} instance.
     * @param url The JDBC URL.
     * @param info Ignored
     * @return the {@link NoopConnection} instance, or {@code null}.
     */
    @Override
    public Connection connect​(final String url,final Properties info)
    {
        if (isOurUrl(url)) return NoopConnection.instance();
        return null;
    }
    @Override
    public boolean acceptsURL​(String url){return isOurUrl(url);}
    @Override
    public int getMajorVersion(){return MockDriver.majorVersion();}
    @Override
    public int getMinorVersion(){return MockDriver.minorVersion();}
    @Override
    public Logger getParentLogger(){return Logger.getLogger(NoopDriver.class.getPackage().getName());}
    @Override
    public DriverPropertyInfo[] getPropertyInfo​(String url, Properties info){return new DriverPropertyInfo[0];}
    @Override
    public boolean jdbcCompliant(){return true;}
    
    private NoopDriver()
    {
        logger.config("NoopDriver version "+MockDriver.majorVersion()+"."+MockDriver.minorVersion());
    }
    
    private static boolean isOurUrl(String url){
        return url.startsWith("jdbc:noop:") || url.equals("jdbc:noop");
    }
}
