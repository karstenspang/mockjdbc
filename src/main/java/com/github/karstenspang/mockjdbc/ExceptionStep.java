package com.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} that throws the exception supplied to the constructor.
 * @param <T> The type returned
 */
public class ExceptionStep<T> implements Step<T> {
    SQLException exception;
    
    /**
     * Construct the step
     * @param exception Exception to be thrown by the step
     */
    public ExceptionStep(SQLException exception)
    {
        this.exception=exception;
    }
    
    /**
     * Throw the supplied exception
     * @param supplier Not used
     * @return None
     * @throws SQLException as requested.
     */
    public T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        throw exception;
    }
    
    /**
     * The string represetation of the step
     * @return the string represetation
     */
    public String toString(){
        return super.toString()+
            "{exception:"+String.valueOf(exception)+"}";
    }
}
