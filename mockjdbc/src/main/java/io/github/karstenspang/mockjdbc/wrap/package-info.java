/**
 * Auto-generated classes that wrap the interfaces in {@link java.sql}.
 * They all extend {@link io.github.karstenspang.mockjdbc.Wrap}.
 * All methods defined in the interface are implemented to call the
 * corresponding method of the wrapped object indirectly in the application of a
 * {@link io.github.karstenspang.mockjdbc.Step}. For example
 * {@link io.github.karstenspang.mockjdbc.wrap.ConnectionWrap#createStatement()}:
 *<pre>
 *&#64;Override
 *public Statement createStatement()
 *    throws SQLException
 *{
 *    return stepSupplier.get().apply(()-&gt;wrapped.createStatement());
 *}
 *</pre>
 * It is up to {@code apply} to execute the method call.
 * In other words, <b>every</b> method call to a wrap causes a 
 * {@link io.github.karstenspang.mockjdbc.Step} to be supplied and applied.
 * However, this does not
 * apply to methods defined in {@link java.lang.Object}, such as {@link java.lang.Object#toString()}.<p>
 * The wraps have constructors that take an object to be wrapped and a
 * {@link java.util.function.Supplier}&lt;{@link io.github.karstenspang.mockjdbc.Step}&gt;,
 * thus matching the signature of
 * {@link io.github.karstenspang.mockjdbc.Wrapper}, for example
 * {@link io.github.karstenspang.mockjdbc.wrap.ConnectionWrap#ConnectionWrap(java.sql.Connection,java.util.function.Supplier)}
 * matches {@link io.github.karstenspang.mockjdbc.Wrapper}&lt;{@link java.sql.Connection}&gt;.
 * For convenience, they also have constructors where the second argument
 * is an {@link Iterable}&lt;{@link io.github.karstenspang.mockjdbc.Step}&gt;.
 * <h3>Exceptions</h3>
 * The vast majority of the methods of the interfaces in {@link java.sql}
 * are declared to throw {@link java.sql.SQLException}, and consequently, the
 * methods in {@link io.github.karstenspang.mockjdbc.Step} are declared likewise. In case a method is not
 * declared to throw {@link java.sql.SQLException}, it is caught and an
 * {@link java.lang.UnsupportedOperationException} is thrown with the original exception
 * as cause.
 * For example, {@link java.sql.Connection#setClientInfo(java.util.Properties)}
 * is declared to throw an {@link java.sql.SQLClientInfoException}. If the
 * {@link io.github.karstenspang.mockjdbc.ExceptionStep} throws an
 * {@link java.sql.SQLClientInfoException}, then that exception is delivered
 * as expected, but if any other {@link java.sql.SQLException} is thrown
 * then an {@link java.lang.UnsupportedOperationException} is delivered.
 * <h3>Methods Defined in {@link java.lang.Object}</h3>
 * The methods defined in {@link java.lang.Object} are not implemented in the
 * generated wraps, even if defined in the interface. The methods
 * {@link java.lang.Object#toString()}, {@link java.lang.Object#hashCode()},
 * and {@link java.lang.Object#equals(java.lang.Object)} are overridden in
 * the base class {@link io.github.karstenspang.mockjdbc.Wrap}.
 */
package io.github.karstenspang.mockjdbc.wrap;
