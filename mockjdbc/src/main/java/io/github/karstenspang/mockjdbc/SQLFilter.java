package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Like {@link UnaryOperator}, but can throw an {@link SQLException}.
 * Meant to be used as a target for lambda expressions.
 * @param <T> the type of the input and the result.
 */
@FunctionalInterface
public interface SQLFilter<T> {
    /**
     * Perform any operation on a value.
     * @param inputValue The data to operate on.
     * @return the filtered value
     * @throws SQLException if needed.
     */
    T apply(T inputValue)
        throws SQLException;
    
    /**
     * Returns a composed filter that first applies the {@code before}
     * filter to its input, and then applies this filter to the result.
     * @param before the filter to apply before this filter is applied
     * @return a composed filter that first applies the {@code before}
     *        filter and then applies this filter.
     * @throws NullPointerException if {@code before} is {@code null}.
     */
    default SQLFilter<T> compose(SQLFilter<T> before)
    {
        Objects.requireNonNull(before);
        return (T t)->apply(before.apply(t));
    }
    
    /**
     * Returns a composed filter that first applies this filter to
     * its input, and then applies the {@code after} filter to the result.
     * @param after the filter to apply after this filter is applied
     * @return a composed filter that first applies this filter and then
     *         applies the {@code after} filter.
     * @throws NullPointerException if {@code after} is {@code null}.
     */
    default SQLFilter<T> andThen(SQLFilter<T> after)
    {
        Objects.requireNonNull(after);
        return (T t)->after.apply(apply(t));
    }
    
    /**
     * Returns a filter that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> SQLFilter<T> identity(){
        return t->t;
    }
}
