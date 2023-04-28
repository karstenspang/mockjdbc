package io.github.karstenspang.mockjdbc;

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WrapTest {
    @Test
    @DisplayName("Passing null values to the constructor causes NullPointerException")
    public void testNull()
    {
        assertThrows(NullPointerException.class,()->new Wrap(Integer.valueOf(1),null),"null program");
        assertThrows(NullPointerException.class,()->new Wrap(null,new Program(Collections.emptyList())),"null program");
    }
    
    @Test
    @DisplayName("hashCode returns the hash code of the wrapped object")
    public void testHash()
    {
        Wrap wrap=new Wrap(Integer.valueOf(1),new Program(Collections.emptyList()));
        assertEquals(1,wrap.hashCode());
    }
    
    @Test
    @DisplayName("equals delegates the wrapped object")
    public void testEquals()
    {
        Wrap wrap=new Wrap(Integer.valueOf(1),new Program(Collections.emptyList()));
        assertTrue(wrap.equals(Integer.valueOf(1)));
    }
    
    @Test
    @DisplayName("equals compares with the wrapped object if the target is a Wrap")
    public void testEqualsWrap()
    {
        Wrap wrap=new Wrap(Integer.valueOf(1),new Program(Collections.emptyList()));
        assertTrue(wrap.equals(new Wrap(Integer.valueOf(1),new Program(Collections.emptyList()))));
    }
    
    @Test
    @DisplayName("toString returns the expected value")
    public void testToString()
    {
        Wrap wrap=new Wrap(Integer.valueOf(1),new Program(Collections.emptyList()));
        String expected=getClass().getPackage().getName()+".Wrap:{wrapped:1,stepSupplier:[]}";
        assertEquals(expected,wrap.toString());
    }
}
