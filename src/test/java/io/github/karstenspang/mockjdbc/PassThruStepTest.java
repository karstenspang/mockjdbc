package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PassThruStepTest {
    
    @Test
    @DisplayName("The same non-null object is returned on every call from instance")
    public void testSameInstance()
    {
        PassThruStep st1=PassThruStep.instance();
        PassThruStep st2=PassThruStep.instance();
        assertNotNull(st1);
        assertSame(st1,st2);
    }
    
    @Test
    @DisplayName("The object is passed through")
    public void testSameObject()
        throws SQLException
    {
        PassThruStep st=PassThruStep.instance();
        final Integer i1=Integer.valueOf(1);
        Integer i2=st.apply(()->i1);
        assertSame(i1,i2);
    }
}
