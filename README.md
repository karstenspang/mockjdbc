[![develop build](https://github.com/karstenspang/mockjdbc/actions/workflows/maven.yml/badge.svg?branch=develop)](https://github.com/karstenspang/mockjdbc/actions/workflows/maven.yml?query=branch%3Adevelop)
[![latest release](https://img.shields.io/github/v/release/karstenspang/mockjdbc?sort=semver)](https://github.com/karstenspang/mockjdbc/releases)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.karstenspang/mockjdbc/badge.svg)](https://mvnrepository.com/artifact/io.github.karstenspang/mockjdbc)
[![javadoc](https://javadoc.io/badge2/io.github.karstenspang/mockjdbc/javadoc.svg)](https://javadoc.io/doc/io.github.karstenspang/mockjdbc)
[![Coverage](.github/badges/jacoco.svg)](https://karstenspang.github.io/mockjdbc/jacoco/index.html)

# mockjdbc
A mock JDBC driver delegating to a real JDBC driver, with the
possibility of simulating errors or modifying return values
for testing purposes. It can also be used for tracing JDBC
calls.

The driver wraps around a real JDBC Driver.
It is controlled by a program, for example
"return a connection the first two times, fail the third,
and turn autocommit on the fourth". For unit testing, the
real driver could be
[the H2 in-memory database](https://www.h2database.com/)
or the built-in [no-op JDBC driver](#no-op-jdbc-driver).
If using H2, you may hit the problem that surefire doesn't
always get the driver loaded. In that case you will need
to load it explicitly in your test code the old-fashioned
`Class.forName` way.
The mock driver will detect if the no-op driver is used and
make sure it is loaded.

The returned connections themselves can be wrapped with
a program, that can cause e.g. `Connection.createStatement()`
to fail on the second attempt. The returned statements again
can be wrapped, etc.

This implementation supports `Driver` only, not `DataSource`.
This means that it does not support interfaces in `javax.sql`.
Nor does it support the interfaces in `java.sql` that depend
`DataSource`, i.e. `ConnectionBuilder`, `ShardingKey`, and
`ShardingKeyBuilder`.

Wraps exist for all other interfaces in `java.sql` for which this is
meaningful, i.e. except `Driver`, `DriverNotification`, and
`Wrapper`.

## Java and JDBC versions
Versions 1.x.x are compiled with JDK 8, and thus support JDBC 4.2.
Versions 2.x.x are compiled with JDK 11, and thus support JDBC 4.3.

The main difference is that the new `default` methods introduced in
JDBC 4.3, e.g. `Statement.executeLargeUpdate`, fully work in 2.x.x.
If using 1.x.x with JDBC 4.3, these methods have their default
implementations in the wraps, even if the wrapped driver has
overridden the default implementations. Also, calls to these methods
are not intercepted.

## Dependency information
To use for unit testing in Maven, add the following to you POM:
```
<dependency>
  <groupId>io.github.karstenspang</groupId>
  <artifactId>mockjdbc</artifactId>
  <version>2.0.1</version>
  <scope>test</scope>
</dependency>
```

## Maven Site
The Maven site can be found on [GitHub Pages](https://karstenspang.github.io/mockjdbc/index.html).

## Examples

Some examples of code that can be tested using mockjdbc. Working code can found in
[`Example.java`](https://github.com/karstenspang/mockjdbc/blob/develop/mockjdbc/src/test/java/io/github/karstenspang/mockjdbc/Example.java).
The test code is in 
[`ExampleTest.java`](https://github.com/karstenspang/mockjdbc/blob/develop/mockjdbc/src/test/java/io/github/karstenspang/mockjdbc/ExampleTest.java).

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

A [Junit 5](https://junit.org/junit5/) test case where the connection
fails once and then succeeds, could be:
```
import io.github.karstenspang.mockjdbc.ExceptionStep;
import io.github.karstenspang.mockjdbc.MockDriver;
import io.github.karstenspang.mockjdbc.PassThruStep;
import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import io.github.karstenspang.mockjdbc.wrap.StatementWrap;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class ConnectorTest {
    @Test
    @DisplayName("If the connection fails once, and then succeeds, you will get a connection")
    public void testOneFailure()
        throws SQLException,InterruptedException
    {
        // Set up a program for MockDriver of two steps, one simulating
        // the error that we want to test, and the second step simply
        // passes the connection request to the wrapped URL.
        SQLException ex=new SQLException("db overloaded","00000",12520);
        ExceptionStep step1=new ExceptionStep(ex);
        PassThruStep step2=PassThruStep.instance();
        MockDriver.setProgram(List.of(step1,step2));
        // Run the test
        Connection conn=Example.getConnection("jdbc:mock:noop:","user","pwd",2,0L);
        conn.close();
    }
}
```
The built-in [no-op JDBC driver](#no-op-jdbc-driver) is used for unit testing,
since we don't need an actual database in this case.

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
    
    List<Step> program=List.of(
        new WrapperStep<Connection>(ConnectionWrap::new,List.of( // Initial connection
            new WrapperStep<Statement>(StatementWrap::new,List.of( // createStatement
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
    Connection conn=Example.checkOrConnect(null,"jdbc:mock:noop:","user","pwd");
    conn=Example.checkOrConnect(conn,"jdbc:mock:noop:","user","pwd");
    conn.close();
    List<LoggingEvent> events=exampleLogger.getLoggingEvents();
    List<LoggingEvent> expectedEvents=List.of(
        LoggingEvent.info("Connecting to jdbc:mock:noop:"),
        LoggingEvent.debug("Checking connection"),
        LoggingEvent.error(disconnect,"Connection broken, closing"),
        LoggingEvent.debug(closeFail,"close failed"),
        LoggingEvent.info("Connecting to jdbc:mock:noop:")
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

Since the compiler can not know which methods will be called in which
order, there is no way to verify that the wraps will have the right type.
If you get it wrong, you will get a `ClassCastException` in the
`WrapperStep` when it calls a wrapper (i.e. wrap constructor) whose
argument type does not match what the method returns. In particular,
it is not possible to wrap the result of a `void` method. In this case,
`InvalidArgumentException` is thrown.

Note that the autogenerated wraps have constructors
taking a wrapped object and a program as arguments, thus matching the
`Wrapper` interface.

[SLF4J Test](https://github.com/valfirst/slf4j-test) is used to check
the log, and thus that the execution took the expected path.

Note that the `ExampleTest` class uses the Junit extension `JulConfigExtension`
from SLF4J Test. This extension ensures that logging from `java.util.logging`
is sent to SLF4J. For some reason, configuring this in `logging.properties` seems
to have no effect when the tests are run from surefire.

### Complex exception handling in `close`

Here is a class implementing `AutoCloseable` with an exhaustive exception
handling in `close()`. The class borrows the connection from the calling
class. This technique is commonly used to ensure that everything happens
in the same transaction.
```
public class UsesConnection implements AutoCloseable {
    final private PreparedStatement st1;
    final private PreparedStatement st2;
    public UsesConnection(Connection conn)
        throws SQLException
    {
        // Create some statements
        st1=conn.prepareStatement("select 1");
        st2=conn.prepareStatement("select 2");
    }
    
    public void doSomething(){
        // Actual code using st1 and st2 goes here
    }
    
    public void close()
        throws SQLException
    {
        SQLException ex=null;
        try{
            st1.close();
        }
        catch(SQLException e){
            ex=e;
        }
        try{
            st2.close();
        }
        catch(SQLException e){
            if (ex==null){
                ex=e;
            }
            else{
                ex.addSuppressed(e);
            }
        }
        if (ex!=null) throw ex;
    }
}
```
The case where `close()` fails on both statements:
```
@Test
@DisplayName("If close() fails for both statements, the exception is from the first with the second suppressed")
public void testClose()
    throws SQLException
{
    try(Connection conn=DriverManager.getConnection("jdbc:noop:","user","pwd")){
        final SQLException ex1=new SQLException("1");
        final SQLException ex2=new SQLException("2");
        final Connection wrappedConnection=new ConnectionWrap(conn,List.of(
            new WrapperStep<PreparedStatement>(PreparedStatementWrap::new,List.of(
                new ExceptionStep(ex1)
            )),
            new WrapperStep<PreparedStatement>(PreparedStatementWrap::new,List.of(
                new ExceptionStep(ex2)
            ))
        ));
        UsesConnection usesConnection=new UsesConnection(wrappedConnection);
        SQLException ex=assertThrows(SQLException.class,()->usesConnection.close());
        assertSame(ex1,ex);
        Throwable[] suppressed=ex.getSuppressed();
        assertEquals(1,suppressed.length);
        assertSame(ex2,suppressed[0]);
    }
}
```
In this example, we already have a connection, but it is wrapped before it is
passed to the `UsesConnection` constructor.

## Trace all JDBC Method Calls

If you need to know what call are made to JDBC, mockjdbc can help you
there as well. You will have to insert this call into your code
```
MockDriver.setStepSupplier(RecursiveWrapperStepSupplier.instance());
```
and of course put the `mock` into your connection string.
The result is that
every JDBC method call will go through a wrap. The effect of this is
that all calls will be logged with level
[`Level.FINEST`](https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/java/util/logging/Level.html#FINEST),
 corresponding to
`TRACE` in most logging backends. If you configure your backend to log
`io.github.karstenspang.mockjdbc` at trace level, you will get a
complete log of all JDBC calls, including arguments and returned results.
You will of course also have to direct `java.util.logging` to the
logging backend of your choice.

## No-op JDBC Driver

A no-op JDBC driver is supplied with mockjdbc.
The methods return dummy values, except for those returning an
interface in `java.sql`. These methods return other no-op objects.
In this way, a scaffold is created on which programs can be put
to create a mock.
For details, please see 
[the javadoc](https://javadoc.io/static/io.github.karstenspang/mockjdbc/2.0.0/io/github/karstenspang/mockjdbc/noop/package-summary.html)
for the package.
