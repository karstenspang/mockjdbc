package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} used when the {@link Program} needs to throw an exception.
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
     * Throw the exception supplied to the constructor.
     * @param supplier Not called
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
