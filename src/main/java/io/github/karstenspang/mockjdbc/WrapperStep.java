package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} that wraps the suplied value.
 * @param <T> The type returned
 */
public class WrapperStep<T> implements Step<T> {
    private Wrapper<T> wrapper;
    
    /**
     * Construct the step.
     * @param wrapper defines how to wrap
     */
    public WrapperStep(Wrapper<T> wrapper){
        this.wrapper=wrapper;
    }
    
    /**
     * Call the supplier, wrap the result, and return the wrap.
     * @param supplier Called to get the result
     * @return wrapped result from {@code supplier}
     * @throws SQLException if {@code supplier} does.
     */
    public T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return wrapper.wrap(supplier.get());
    }
}
