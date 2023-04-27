/**
 * A mock JDBC driver delegating to a real JDBC driver,
 * with the possibility of simulating errors.<p>
 * The driver is a wrap around a real JDBC Driver.
 * The driver is controlled by a program, for example
 * "return a connection the first two times, fail on the third".
 * The returned connections themselves are wraps controlled
 * by a program, that can cause e.g.
 * {@link java.sql.Connection#createStatement()}
 * to fail on the second attempt. The returned statements are
 * again wraps around {@link java.sql.Statement} controlled by a program, etc.
 * <h3>Programs, Steps, and Wraps</h3>
 * A program is simply a list of steps to be run in the specified order
 * when a step is needed. Programs can be put on the
 * {@link io.github.karstenspang.mockjdbc.MockDriver},
 * or on wraps. A step is an implementation of the interface
 * {@link io.github.karstenspang.mockjdbc.Step}.
 * The following implementations are defined:
 * <ul>
 *  <li><code>PassThruStep</code> passes the method call on to the
 *      wrapped driver and returns the result.</li>
 *  <li><code>Excpetionstep</code> throws an exception. The constructor
 *      takes the exception to throw as an argument. It must
 *      be an instance of <code>SQLException</code>
 *      or <code>RuntimeException</code>.</li>
 *  <li><code>WrapperStep</code> passes the method call on to the wrapped
 *      driver, then wraps the result with a program, and returns the wrap.
 *      The constructor takes a <code>Wrapper</code> and program as
 *      arguments.
 *      The wrapper is usually a function reference to the
 *      construtor of a wrap, e.g. <code>ConnectionWrap::new</code>.
 *      The program is an <code>Iterable&lt;Step&gt;</code>,
 *      usually a <code>List</code>, since you need the steps to be in
 *      a predictable sequence. You may of course also write your own
 *      <code>Iterable&lt;Step&gt;</code> doing something intelligent.</li>
 * </ul>
 * The {@link io.github.karstenspang.mockjdbc.MockDriver}
 * takes the next step from the program whenever
 * it needs to delegate connection creation to the wrapped class.
 * <h3>Autogenerated Wraps</h3>
 * Wraps around the interfaces in {@link java.sql} are included.
 * They reside in the
 * <a href="wrap/package-summary.html"><code>io.github.karstenspang.mockjdbc.wrap</code></a>
 * package.
 * <h3>Wrap or Wrapper</h3>
 * It has become custom to denote something which is wrapped around
 * something a "wrapper". This is not correct
 * English; strictly speaking such a thing is called a "wrap", and
 * a "wrapper" is a person or machine (or piece of software) that puts
 * the wrap on.
 * <p>
 * Since this software has both classes with the role "is a
 * wrap of something" and classes with the role "puts a wrap on
 * something", the former have "Wrap" as part of their name,
 * while the latter have "Wrapper" as part of their name.
 * <h3>Logging</h3>
 * In accordance with the standard for JDBC,
 * the driver uses {@link java.util.logging}.
 * All loggers are children of the logger named
 * {@code io.github.karstenspang.mockjdbc}.
 * The following is logged:
 * <ul>
 *  <li>When the driver is loaded, it logs its version with level <code>CONFIG</code>.</li>
 *  <li>When a program is set in the driver, or a wrap is created, the program
 *      is logged with level <code>FINE</code>.</li>
 *  <li>When a step is applied, it is logged with level <code>FINEST</code>.</li>
 * </ul>
 */
package io.github.karstenspang.mockjdbc;
