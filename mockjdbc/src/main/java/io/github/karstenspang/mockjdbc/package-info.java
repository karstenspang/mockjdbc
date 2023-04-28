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
 * A program is a {@link java.util.function.Supplier} of steps.
 * The {@link Program} class 
 * is based on a predefined list of steps to be run in the specified order
 * when a step is needed. Programs can be put on the
 * {@link io.github.karstenspang.mockjdbc.MockDriver},
 * or on wraps. A step is an implementation of the interface
 * {@link io.github.karstenspang.mockjdbc.Step}.
 * The following implementations are defined:
 * <ul>
 *  <li><code>PassThruStep</code> passes the method call on to the
 *      wrapped driver and returns the result.</li>
 *  <li><code>Exceptionstep</code> throws an exception. The constructor
 *      takes the exception to throw as an argument. It must
 *      be an instance of <code>SQLException</code>
 *      or <code>RuntimeException</code>.</li>
 *  <li><code>WrapperStep</code> passes the method call on to the wrapped
 *      driver, then wraps the result with a program, and returns the wrap.
 *      The constructors take a <code>Wrapper</code> and program as
 *      arguments.
 *      The wrapper is usually a function reference to the
 *      construtor of a wrap, e.g. <code>ConnectionWrap::new</code>.
 *      Two constructor variations exist. In the first, the program
 *      is an <code>Iterable&lt;Step&gt;</code>.
 *      This is usually a <code>List</code>, since you need the steps to be in
 *      a predictable sequence.
 *      In the other, the program is a <code>Supplier&lt;Step&gt;</code>,
 *      allowing you to write your own implementation doing something
 *      intelligent.</li>
 *  <li><code>ConstantStep</code> returns a constant value instead of
 *      executing the method.</li>
 *  <li><code>SupplierStep</code> returns the value of a function instead of
 *      executing the method.</li>
 *  <li><code>RunnableStep</code> executes some action instead of
 *      executing the method.</li>
 *  <li><code>FilterStep</code> executes the method and performs some
 *      transformation of the result. <code>WrapperStep</code> is a
 *      specialized <code>FilterStep</code>.</li>
 * </ul>
 * The {@link io.github.karstenspang.mockjdbc.MockDriver}
 * takes a step from the program whenever
 * it needs to delegate connection creation to the wrapped class.
 * <h3>Autogenerated Wraps</h3>
 * Wraps around the interfaces in {@link java.sql} are included.
 * They reside in the
 * {@link io.github.karstenspang.mockjdbc.wrap} package.
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
 *  <li>When a step supplier is set in the driver, or a wrap is created, the supplier
 *      is logged with level <code>FINE</code>.</li>
 *  <li>Before a step is applied, it is logged with level <code>FINEST</code>.
 *      If a result is returned from the step, it is logged as well.</li>
 * </ul>
 */
package io.github.karstenspang.mockjdbc;
