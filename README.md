[![develop build](https://github.com/karstenspang/mockjdbc/actions/workflows/maven.yml/badge.svg?branch=develop)](https://github.com/karstenspang/mockjdbc/actions/workflows/maven.yml?query=branch%3Adevelop)
[![latest release](https://img.shields.io/github/v/release/karstenspang/mockjdbc?sort=semver)](https://github.com/karstenspang/mockjdbc/releases)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.karstenspang/mockjdbc/badge.svg)](https://mvnrepository.com/artifact/io.github.karstenspang/mockjdbc)
[![javadoc](https://javadoc.io/badge2/io.github.karstenspang/mockjdbc/javadoc.svg)](https://javadoc.io/doc/io.github.karstenspang/mockjdbc)

# mockjdbc
A mock JDBC driver delegating to a real JDBC driver, with the
possibility of simulating errors for testing purposes.

The driver is a wrapper around a real JDBC Driver, simulating SQL errors.
The driver is controlled by a program, for example
"return a connection the first two times, fail on the third".

The returned connections themselves can be wrappers controlled by
a program, that can cause e.g. `Connection.createStatement()`
to fail on the second attempt. The returned statements again
can be wrappers around `Statement` controlled by a program, etc.
Currently wrappers exist for `Connection`, `Statement`,
`PreparedStatement`, and `CallableStatement`.

## Java and JDBC versions
For maximum usefullness,
I have decided to build this with Java 8, and thus the wrappers
will implement only what is in JDBC 4.2. If used with java 9 and
higher, new methods introduced in 4.3 will have their default
implementation in the wrappers, even if the wrapped driver has
overridden the default implementation.

## Dependency information
To use for unit testing in Maven, add the following to you POM:
```
<dependency>
  <groupId>io.github.karstenspang</groupId>
  <artifactId>mockjdbc</artifactId>
  <version>0.1.2</version>
  <scope>test</scope>
</dependency>
```
## Examples

Some examples of code that can be tested using mockjdbc. These can be found in
`Example.java` in the test sources.

For details, see [the javadoc](https://javadoc.io/doc/io.github.karstenspang/mockjdbc).

### Retry in case of overloaded database
Let's say you have an Oracle database that sometimes have too many
connections, and for that reason, connecting fails with
```
ORA-12520: TNS:listener Could Not Find Available Handler For Requested Type Of Server
```
Now you want to handle this by waiting a bit and trying again if this error
is encountered, but not too many times. This could e.g. be
```
public class Connector {
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
}
```
So, how to test this? You could of course arrange the database to have too
many connections, and then try to connect, but good luck automating that!

A Junit 5 test case where the connection fails once and then
succeeds, could be:
```
import io.github.karstenspang.mockjdbc.ExceptionStep;
import io.github.karstenspang.mockjdbc.MockDriver;
import io.github.karstenspang.mockjdbc.PassThruStep;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
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
        ExceptionStep step1=new ExceptionStep(ex);
        PassThruStep step2=PassThruStep.instance();
        MockDriver.setProgram(Arrays.asList(step1,step2));
        // Run the test
        Connection conn=Example.getConnection("jdbc:mock:h2:mem:","user","pwd",2,0L);
        conn.close();
    }
}
```
[The H2 in-memory database](https://www.h2database.com/)
is used for unit testing, since we don't need
an actual database in this case. It seems that surefire don't always get
the driver loaded, so we do this explicitly.

It is not neccessary to include the second step, since the program will
return a `PassThruStep` if called after the list of steps is exhausted.

### Reconnect if connection is broken

If you have problems with the connection being broken, e.g. to
firewall timeouts, you can remedy this by testing the connection
at strategic points in the code and reconnect if needed. This method
could do the job. The code has SLF4J logging, allowing verification of
what was actually done.
```
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
```
Here, you want either `conn.createStatement` or `stmt.execute` to fail, so you
can force the reconnection. This case is when you create the connection
at the first attempt, but the check fails on `stmt.execute`
in the second attempt, and reconnection is successfull.
```
@Test
@DisplayName("If the connection was broken, it is reconnected")
public void testCheckConnection()
    throws SQLException
{
    SQLException disconnect=new SQLException("Connection broken");
    SQLException closeFail=new SQLException("close failed");
    
    List<Step> program=Arrays.asList(
        new WrapperStep<Connection>(ConnectionWrap::new,Arrays.asList( // Initial connection
            new WrapperStep<Statement>(StatementWrap::new,Arrays.asList( // createStatement
                new ExceptionStep(disconnect), // execute
                PassThruStep.instance() // Statement.close
            )),
            new ExceptionStep(closeFail) // Connection.close
        )),
        PassThruStep.instance() // Reconnect
    );
    
    TestLogger exampleLogger=TestLoggerFactory.getTestLogger(Example.class);
    exampleLogger.clear();
    MockDriver.setProgram(program);
    Connection conn=Example.checkOrConnect(null,"jdbc:mock:h2:mem:","user","pwd");
    conn=Example.checkOrConnect(conn,"jdbc:mock:h2:mem:","user","pwd");
    conn.close();
    List<LoggingEvent> events=exampleLogger.getLoggingEvents();
    List<LoggingEvent> expectedEvents=Arrays.asList(
        LoggingEvent.info("Connecting to jdbc:mock:h2:mem:"),
        LoggingEvent.debug("Checking connection"),
        LoggingEvent.error(disconnect,"Connection broken, closing"),
        LoggingEvent.debug(closeFail,"close failed"),
        LoggingEvent.info("Connecting to jdbc:mock:h2:mem:")
    );
    assertEquals(expectedEvents,events);
}
```
The first step of the program creates a wrap around the connection, giving it
a program of its own. Every method call on the connection will have a step
applied in the order give by the program.
The program on the connection creates another wrap around the statement
returned by `createStatement`, makes `close` fail, and lets the reconnect succeed.
The program on the statement makes `execute` fail, but `close` succeed.

Note that the autogenerated wraps have constructors
taking a wrapped object and a program as arguments, thus matching the
`Wrapper` interface.

[SLF4J Test](http://projects.lidalia.org.uk/slf4j-test/) is used to check
the log, and thus that the execution took the expected path.
