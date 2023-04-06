package com.github.karstenspang.mockjdbc;

import java.util.logging.Level;
import java.util.logging.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class TestLogging {
    
    // Redirect all logging from JUL to SLF4J
    public static void setup()
    {
        if (!SLF4JBridgeHandler.isInstalled()){
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
        }
    }
}
