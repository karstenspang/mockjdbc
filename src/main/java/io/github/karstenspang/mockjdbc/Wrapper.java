package io.github.karstenspang.mockjdbc;

/**
 * A functional that puts a wrap on something.
 * @param <W> The type of both the wrap and the wrapped,
 * usually an interface that they both implement.
 */
@FunctionalInterface
public interface Wrapper<W>{
    /**
     * Wrap an object.
     * @param wrapped Object to be wrapped.
     * @param program The program for the wrap.
     * @return the wrap of the object.
     */
    W wrap(W wrapped,Program program);
}
