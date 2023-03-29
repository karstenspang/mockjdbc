# mockjdbc
A mock JDBC driver delegating to a real JDBC driver, with the
possibility of simulating errors.

**To be implemented**

The driver is a wrapper around a real JDBC Driver, simulating SQL errors.
The driver is controlled by a program, for example
"return a connection the first two times, fail on the third".
The returned connections themselves are wrappers controlled by
a program, that can cause e.g. `Connection.createStatement()`
to fail on the second attempt. The returned statements are again
wrappers around `Statement` controlled by a program, etc.
