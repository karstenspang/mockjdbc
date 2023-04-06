package com.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProgramTest {
    
    @Test
    @DisplayName("Program with one exception returns one exception and then passthru")
    public void testOneException()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        program.toString();
        Iterator<Step<Connection>> steps=program.iterator();
        assertInstanceOf(ExceptionStep.class,steps.next());
        assertInstanceOf(PassThruStep.class,steps.next());
        assertInstanceOf(PassThruStep.class,steps.next());
    }
    
    @Test
    @DisplayName("Program with one exception forEach returns one element")
    public void testOneExceptionForEach()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        final AtomicInteger cnt=new AtomicInteger();
        program.forEach((c)->{cnt.incrementAndGet();});
        assertEquals(1,cnt.get());
    }
    
    @Test
    @DisplayName("Program with one exception forEachRemaining returns one element")
    public void testOneExceptionForEachRemaining()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        final AtomicInteger cnt=new AtomicInteger();
        program.iterator().forEachRemaining((c)->{cnt.incrementAndGet();});
        assertEquals(1,cnt.get());
    }
    
    @Test
    @DisplayName("Program with one exception spliterator returns one element")
    public void testOneExceptionSpliterator()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        assertEquals(1,StreamSupport.stream(program.spliterator(),false).count());
    }
    
    @Test
    @DisplayName("Program with one exception hasNext returns one element")
    public void testOneExceptionhasNext()
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        int cnt=0;
        for (Step<Connection> s:program){
            cnt++;
        }
        assertEquals(1,cnt);
    }
}
