package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link Step} that filters the value returned by the method.
 * This is done by a {@link SQLFilter} that can perform
 * any transformation on the result.
 * One example could be a filter that takes a {@link Connection} and
 * sets some attribute on it, and then passes it on. That filter could
 * further be composed with a {@link WrapperFilter} to wrap the
 * connection before it is returned.
 * @param <F> The filtered type.
 */
public class FilterStep<F> implements Step {
    private SQLFilter<F> filter;
    
    /**
     * Construct the step, storing the filter for use in {@link #apply(SQLSupplier)}.
     * @param filter Filter to apply to the result from the method call in {@link #apply(SQLSupplier)}.
     * @throws NullPointerException if {@code filter} is {@code null}.
     */
    public FilterStep(SQLFilter<F> filter){
        this.filter=Objects.requireNonNull(filter);
    }
    
    /**
     * Call the method, filter the result, and return the filtered value.
     * @param <T> The type returned by {@code method} and this method. Must match <code>&lt;F&gt;</code>.
     * @param method Called to get the object to filter.
     * @return filtered value.
     * @throws SQLException if either {@code supplier} or the filter does.
     * @throws ClassCastException if <code>&lt;F&gt;</code> and <code>&lt;T&gt;</code> do not match.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(SQLSupplier<? extends T> method)
        throws SQLException
    {
        return (T)filter.apply((F)method.get());
    }
    
    /**
     * Always throws {@link UnsupportedOperationException}, as there is no way to filter {@code void}.
     * @param method not used
     * @throws UnsupportedOperationException always
     */
    @Override
    public void apply(SQLRunnable method)
    {
        throw new UnsupportedOperationException("void cannot be filtered");
    }
    
    @Override
    public String toString(){
        return "FilterStep";
    }
}
