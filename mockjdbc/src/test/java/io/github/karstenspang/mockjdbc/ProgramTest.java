package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProgramTest {
    
    @Test
    @DisplayName("Program with one exception returns one exception and then passthru")
    public void testOneException()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep step=new ExceptionStep(myex);
        Program program=new Program(List.of(step));
        program.toString();
        assertInstanceOf(ExceptionStep.class,program.get());
        assertInstanceOf(PassThruStep.class,program.get());
        assertInstanceOf(PassThruStep.class,program.get());
    }
}
