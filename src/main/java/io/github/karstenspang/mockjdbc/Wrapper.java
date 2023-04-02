package io.github.karstenspang.mockjdbc;

/**
 * A functional that puts a wrap on something.
 * @param <T> The type of both the wrap and the wrapped,
 * for example an interface that they both implement.
 */
public interface Wrapper<T>{
    /**
     * Wrap an object.
     * @param wrapped Object to be wrapped.
     * @return the wrap of the object.
     */
    T wrap(T wrapped);
}
