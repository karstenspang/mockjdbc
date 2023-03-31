package com.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} that returns the suplied value.
 * @param <T> The type returned
 */
public class PassThruStep<T> implements Step<T> {
    private static PassThruStep<?> instance=new PassThruStep<>();
    
    /**
     * Get (the only) instance.
     * @return the instance
     */
    @SuppressWarnings("unchecked")
    public static <U> PassThruStep<U> instance(){
        return (PassThruStep<U>)instance;
    }
    
    /**
     * Call the supplier and return the result.
     * @param supplier Called to get the result
     * @return the result from {@code supplier}
     * @throws SQLException if {@code supplier} does.
     */
    public T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return supplier.get();
    }
    
    private PassThruStep(){}
}
