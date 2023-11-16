package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import io.github.karstenspang.mockjdbc.ExceptionStep;
import io.github.karstenspang.mockjdbc.MockDriver;
import io.github.karstenspang.mockjdbc.PassThruStep;
import io.github.karstenspang.mockjdbc.wrap.PreparedStatementWrap;
import io.github.karstenspang.mockjdbc.wrap.StatementWrap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WrapperStepTest {
    @Test
    @DisplayName("Superclass of wrap throws ClassCastException")
    public void testUp()
        throws SQLException
    {
        MockDriver.setProgram(List.of(
            new WrapperStep<Connection>(ConnectionWrap::new,List.of(
                new WrapperStep<Statement>(StatementWrap::new,List.of())
            ))
        ));
        
        try(Connection conn=DriverManager.getConnection("jdbc:mock:noop:","user","pwd")){
            assertThrows(ClassCastException.class,()->conn.prepareStatement("select 0"));
        }
    }
    
    @Test
    @DisplayName("Subclass of wrap throws ClassCastException")
    public void testDown()
        throws SQLException
    {
        MockDriver.setProgram(List.of(
            new WrapperStep<Connection>(ConnectionWrap::new,List.of(
                new WrapperStep<PreparedStatement>(PreparedStatementWrap::new,List.of())
            ))
        ));
        
        try(Connection conn=DriverManager.getConnection("jdbc:mock:noop:","user","pwd")){
            assertThrows(ClassCastException.class,()->conn.createStatement());
        }
    }
}
