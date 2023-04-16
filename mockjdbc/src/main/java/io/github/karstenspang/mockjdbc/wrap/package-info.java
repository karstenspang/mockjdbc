/**
 * Auto-generated classes that wrap the interfaces in {@link java.sql}.
 * All methods defined in the interface is implemented to call the
 * method wrapped object, after application of a {@link Step}, for
 * example {@link ConnectionWrap#createStatement()}:
 *<pre>
 *&#64;Override
 *public Statement createStatement()
 *    throws SQLException
 *{
 *    return steps.next().apply(()-&gt;wrapped.createStatement());
 *}
 *</pre>
 * In other words, <b>every</b> method call to a wraps causes the program
 * to be advanced to the next step.
 * They have a constructor that take an object to be wrapped and a
 * {@link Program}, thus matching the signature of {@link Wrapper},
 * like {@link ConnectionWrap#ConnectionWrap(Connection,Program)}.
 */
package io.github.karstenspang.mockjdbc.wrap;

import io.github.karstenspang.mockjdbc.Program;
import io.github.karstenspang.mockjdbc.Step;
import io.github.karstenspang.mockjdbc.Wrapper;
import java.sql.Connection;
