package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {
    private static Logger logger=LoggerFactory.getLogger(Example.class);
    public static Connection getConnection(
        String url,
        String user,
        String password,
        int maxFails,
        long sleepTime
    )
        throws SQLException,InterruptedException
    {
        int fails=0;
        while(true){
            try{
                return DriverManager.getConnection(url,user,password);
            }
            catch(SQLException ex){
                if (fails>maxFails) throw ex;
                int code=ex.getErrorCode();
                if (code!=12520) throw ex;
            }
            fails++;
            Thread.sleep(sleepTime);
        }
    }
    
    public static Connection checkOrConnect(
        Connection conn,
        String url,
        String user,
        String password
    )
        throws SQLException
    {
        if (conn!=null){
            logger.debug("Checking connection");
            try(Statement stmt=conn.createStatement()){
                stmt.execute("select 0");
            }
            catch(SQLException e){
                logger.error("Connection broken, closing",e);
                try{
                    conn.close();
                }
                catch(SQLException ee){
                    logger.debug("close failed",ee);
                }
                conn=null;
            }
        }
        if (conn==null){
            logger.info("Connecting to "+url);
            conn=DriverManager.getConnection(url,user,password);
        }
        return conn;
    }
}
