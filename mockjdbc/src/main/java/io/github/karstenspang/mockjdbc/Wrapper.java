package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import java.util.function.Supplier;

/**
 * A functional that puts a {@link Wrap} on something.
 * Method references to the constructors of the auto-generated
 * wraps in {@link io.github.karstenspang.mockjdbc.wrap},
 * e.g. {@link ConnectionWrap}{@code ::new}, all
 * match this interface.
 * @param <W> The type of both the wrap and the wrapped,
 *            meant to be an interface in {@link java.sql}
 *            that they both implement.
 */
@FunctionalInterface
public interface Wrapper<W>{
    /**
     * Wrap an object.
     * @param wrapped Object to be wrapped.
     * @param stepSupplier The supplier of steps for the wrap.
     * @return the wrap of the object.
     */
    W wrap(W wrapped,Supplier<Step> stepSupplier);
}
