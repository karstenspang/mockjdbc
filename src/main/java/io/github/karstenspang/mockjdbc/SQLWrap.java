package io.github.karstenspang.mockjdbc;

import java.sql.Wrapper;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Base class for wrappers of interfaces in {@code java.sql}.
 * These all extend {@link AutoCloseable}, with exceptions limited 
 * to {@link SQLException}, and {@link Wrapper}.
 */
public class SQLWrap implements AutoCloseable,Wrapper {
    /** The wrapped object */
    protected final Object wrapped;
    /** Steps to apply */
    protected final Iterator<Step> steps;
    /**
     * Wrap an object
     * @param wrapped Object to wrap
     * @param program Program to wrap the object with
     */
    protected SQLWrap(Object wrapped,Program program){
        this.wrapped=wrapped;
        this.steps=program.iterator();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void close()
        throws SQLException
    {
        try{
            ((AutoCloseable)wrapped).close();
        }
        catch (SQLException|RuntimeException e){
            throw e;
        }
        catch(Exception e){
            throw new IllegalArgumentException("Unexpected exception",e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean isWrapperFor​(Class<?> iface)
        throws SQLException
    {
        return ((Wrapper)wrapped).isWrapperFor(iface);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap​(Class<T> iface)
        throws SQLException
    {
        return ((Wrapper)wrapped).unwrap(iface);
    }
}
