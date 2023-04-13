package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.ExceptionStep;
import io.github.karstenspang.mockjdbc.MockDriver;
import io.github.karstenspang.mockjdbc.PassThruStep;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WrapperStepTest {

    @Test
    @DisplayName("Wrapping a void method throws IllegalArgumentException")
    public void testWrapVoid()
    {
        WrapperStep<Object> step=new WrapperStep<>(Wrap::new,Collections.emptyList());
        assertThrows(IllegalArgumentException.class,()->step.apply(()->{}));
    }
}
