package com.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * The basic step in a mock {@link Program}.
 * @param <T> The type returned
 */
@FunctionalInterface
public interface Step<T> {
    /**
     * Apply the action of the step. Example actions are
     * <ul>
     * <li>Call <code>supplier</code> and return the result.</li>
     * <li>Call <code>supplier</code> and wrap the result, then return the wrapper.</li>
     * <li>Return something else.</li>
     * <li>Throw an exception.</li>
     * </ul>
     * @param supplier Supplier of the result that may or may not be called.
     * @throws SQLException if either {@code supplier}, or if the step itself does.
     */
    T apply(SQLSupplier<? extends T> supplier)
        throws SQLException;
}
