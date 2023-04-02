/**
 * A mock JDBC driver delegating to a real JDBC driver,
 * with the possibility of simulating errors.<br>
 * The driver is a wrap around a real JDBC Driver.
 * The driver is controlled by a program, for example
 * "return a connection the first two times, fail on the third".
 * The returned connections themselves are wraps controlled
 * by a program, that can cause e.g. {@link Connection#createStatement()}
 * to fail on the second attempt. The returned statements are
 * again wraps around {@link Statement} controlled by a program, etc.
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
 * the driver uses {@code java.util.logging}.
 * All loggers are children of the logger named
 * {@code com.github.karstenspang.mockjdbc}.
 */
package com.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.Statement;
