package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The basic step in a mock {@link Program}.
 */
public interface Step {
    /**
     * Apply the action of the step. Example actions are
     * <ul>
     * <li>Call <code>supplier</code> and return the result.</li>
     * <li>Call <code>supplier</code> and wrap the result, then return the wrap.</li>
     * <li>Return something else, perhaps {@code null}.</li>
     * <li>Throw an exception.</li>
     * </ul>
     * @param <T> The type of data returned by the step.
     * @param supplier Supplier of the result. It may or may not be called,
     *        depending on the implementing step.
     * @return Whatever the step decides.
     * @throws SQLException if either {@code supplier} does, or if the step itself does.
     */
    <T> T apply(SQLSupplier<? extends T> supplier)
        throws SQLException;
}
