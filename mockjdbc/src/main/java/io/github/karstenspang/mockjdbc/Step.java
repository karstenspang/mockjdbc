package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The basic step in a mock program.
 * Steps are applied to method calls in the wrapped object.
 * The calls are functionals passed to the proper {@code apply} method.
 */
public interface Step {
    /**
     * Apply the step action to a wrapped method that returns a result.
     * The method call is represented as an {@link SQLSupplier} defined in the
     * {@link Wrap} to call the method in the wrapped object and return its result.
     * The return type of the method must match {@code T}.
     * If the wrapped method returns a primitive type, the corresponding object
     * type is used.
     * <p>
     * Example actions are
     * <ul>
     * <li>Call <code>method</code> and return the result.</li>
     * <li>Call <code>method</code> and wrap the result, then return the wrap.</li>
     * <li>Return something else, perhaps {@code null}.</li>
     * <li>Throw an exception.</li>
     * </ul>
     * @param <T> The type of data returned by the step.
     * @param method Wrapped method call.
     *        It may or may not be called, depending on the implementing step.
     * @return Whatever the step decides.
     * @throws SQLException if either {@code method} does, or if the step itself does.
     */
    <T> T apply(SQLSupplier<? extends T> method)
        throws SQLException;
    
    /**
     * Apply the step action to a wrapped method call that does not return a result.
     * The method call is represented as an {@link SQLRunnable} defined in the
     * {@link Wrap} to call the method in the wrapped object.
     * <p>
     * Example actions are
     * <ul>
     * <li>Call <code>method</code>.</li>
     * <li>Do something else, or nothing.</li>
     * <li>Throw an exception.</li>
     * </ul>
     * @param method Wrapped method call. 
     *        It may or may not be called, depending on the implementing step.
     * @throws SQLException if either {@code method} does, or if the step itself does.
     */
    void apply(SQLRunnable method)
        throws SQLException;
}
