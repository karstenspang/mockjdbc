package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FilterStepTest {
    
    @Test
    @DisplayName("Filtering with identity returns the input")
    public void testFilterIdentity()
        throws SQLException
    {
        FilterStep<Object> step=new FilterStep<>(SQLFilter.identity().andThen(SQLFilter.identity()).compose(SQLFilter.identity()));
        assertEquals("FilterStep",step.toString());
        Object x=new Object();
        Object y=step.apply(()->x);
        assertSame(x,y);
    }
    
    @Test
    @DisplayName("Filtering a void method throws UnsupportedOperationException")
    public void testFilterVoid()
    {
        FilterStep<Object> step=new FilterStep<Object>(SQLFilter.identity());
        assertThrows(UnsupportedOperationException.class,()->step.apply(()->{}));
    }
}
