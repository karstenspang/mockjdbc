package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Step that returns a value from a stored {@link SQLSupplier}, rather than the one in the wrap.
 */
public class SupplierStep implements Step {
    private SQLSupplier<?> supplier;
    
    /**
     * Construct the step.
     * @param supplier is stored for use by {@link #apply(SQLSupplier)}.
     * @throws NullPointerException if {@code supplier} is {@code null}.
     */
    public SupplierStep(SQLSupplier<?> supplier){
        this.supplier=Objects.requireNonNull(supplier,"supplier is null");
    }
    
    /**
     * Return the value from the stored supplier.
     * @param <T> The type returned by this method.
     * @param supplier Not used.
     * @return the value returned by the supplier stored by the constructor.
     * @throws SQLException if the stored supplier does.
     * @throws ClassCastException if the value returned by the 
     *         stored supplier can not be cast to <code>&lt;T&gt;</code>.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return (T)this.supplier.get();
    }
    
    /**
     * Always throws {@link UnsupportedOperationException}.
     * @param action not used
     * @throws UnsupportedOperationException always
     */
    @Override
    public void apply(SQLRunnable action)
    {
        throw new UnsupportedOperationException("value can not be returned");
    }
    
    @Override
    public String toString(){
        return "SupplierStep";
    }
}
