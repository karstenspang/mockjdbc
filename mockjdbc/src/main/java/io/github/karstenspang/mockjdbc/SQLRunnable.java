package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * Like {@link Runnable}, but can throw an {@link SQLException}.
 * Meant to be used as a target for lambda expressions.
 */
@FunctionalInterface
public interface SQLRunnable {
    /**
     * Perform the action.
     * @throws SQLException if needed.
     */
    void run()
        throws SQLException;
}
