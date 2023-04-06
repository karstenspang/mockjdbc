package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Like {@link Supplier}, but can throw an {@link SQLException}.
 * Meant to be used as a target for lambda expressions.
 * @param <T> the type supplied
 */
@FunctionalInterface
public interface SQLSupplier<T> {
    /**
     * Get a value
     * @return the value
     * @throws SQLException if needed.
     */
    T get()
        throws SQLException;
}
