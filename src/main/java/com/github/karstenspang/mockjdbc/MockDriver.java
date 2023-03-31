package com.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 
 */
public class MockDriver implements Driver {
    private static final Logger logger=Logger.getLogger(MockDriver.class.getName());
    private static final Driver instance;
    static{
        instance=new MockDriver();
        try{
            DriverManager.registerDriver(instance);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public Connection connect​(String url,Properties info){return null;}
    @Override
    public boolean acceptsURL​(String url){return url.startsWith("jdbc:mock:");}
    @Override
    public int getMajorVersion(){return 0;}
    @Override
    public int getMinorVersion(){return 0;}
    @Override
    public Logger getParentLogger(){return Logger.getLogger(MockDriver.class.getPackage().getName());}
    @Override
    public DriverPropertyInfo[] getPropertyInfo​(String url, Properties info){return new DriverPropertyInfo[0];}
    @Override
    public boolean jdbcCompliant(){return true;}
    private MockDriver(){}
}