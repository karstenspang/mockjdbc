/**
 * A mock JDBC driver delegating to a real JDBC driver,
 * with the possibility of simulating errors.<br>
 * The driver is a wrapper around a real JDBC Driver.
 * The driver is controlled by a program, for example
 * "return a connection the first two times, fail on the third".
 * The returned connections themselves are wrappers controlled
 * by a program, that can cause e.g. {@link Connection#createStatement()}
 * to fail on the second attempt. The returned statements are
 * again wrappers around Statement controlled by a program, etc.
 * <h3>Logging</h3>
 * In accordance with the standard for JDBC,
 * the driver uses {@code java.util.logging}.
 * All loggers are children of the logger named
 * {@code com.github.karstenspang.mockjdbc}.
 */
package com.github.karstenspang.mockjdbc;
