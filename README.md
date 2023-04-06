# mockjdbc
A mock JDBC driver delegating to a real JDBC driver, with the
possibility of simulating errors. 

The driver is a wrapper around a real JDBC Driver, simulating SQL errors.
The driver is controlled by a program, for example
"return a connection the first two times, fail on the third".

**To be implemented**
The returned connections themselves are wrappers controlled by
a program, that can cause e.g. `Connection.createStatement()`
to fail on the second attempt. The returned statements are again
wrappers around `Statement` controlled by a program, etc.

## Example
Let's say you have an Oracle database that sometimes have too many
connections, and for that reason, connecting fails with
```
ORA-12520: TNS:listener Could Not Find Available Handler For Requested Type Of Server
```
Now you want to handle this by waiting a bit and trying again if this error
is encountered, but not too many times. This could e.g. be
```
public class Connector {
    public static Connection getConnection(String url,String user,String password,int maxFails,long sleepTime)
        throws SQLException,InterruptedException
    {
        int fails=0;
        while(true){
            try{
                return DriverManager.getConnection(url,user,password);
            }
            catch(SQLException ex){
                if (fail>maxFails) throw ex;
                int code=ex.getErrorCode();
                if (code!=12520) throws ex;
            }
            fails++;
            Thread.sleep(sleepTime);
        }
    }
}
```
So, how to test this? You could of course arrange the database to have too
many connecions, and then try to connect, but good luck automating that!

A Junit 5 test case that tests that if the connection fails once and then
succeeds, you will get a connection could be:
```
import com.github.karstenspang.mockjdbc.ExceptionStep;
import com.github.karstenspang.mockjdbc.MockDriver;
import com.github.karstenspang.mockjdbc.passThruStep;
import com.github.karstenspang.mockjdbc.Program;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class ConnectorTest {
    @BeforeAll
    static void init()
        throws ClassNotFoundException
    {
        // Make sure the H2 driver is loaded.
        Class.forName("org.h2.Driver");
    }
    
    @Test
    @DisplayName("If the connection fails once, and then succeeds, you will get a connection")
    public void testOneFailure()
        throws SQLException,InterruptedException
    {
        // Set up a program for MockDriver of two steps, one simulating
        // the error that we want to test, and the second step simply
        // passes the connction request to the wrapped URL.
        SQLException ex=new SQLException("db overloaded","00000",12520);
        ExceptionStep<Connection> step1=new ExceptionStep<>(ex);
        PassThruStep<Connection> step2=PassThruStep.instance();
        Program<Connection> program=new Program<>(List.of(step1,step2));
        MockDriver.setProgram(program);
        // Run the test
        try(Connection conn=Connector.getConnection("jdbc:mock:h2:mem:","user","pwd",2,0L)){}
    }
}
```
The H2 in-memory database is used for unit testing, since we don't need
an actual database in this case. It seems that surefire don't always get
the driver loaded, so we do this explicitly.

It is not neccessary to include the second step, since the program will
return a `PassThruStep` if called after the list of steps is exhausted.
