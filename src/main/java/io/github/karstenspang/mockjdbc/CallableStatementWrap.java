package io.github.karstenspang.mockjdbc;

import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class CallableStatementWrap<S extends CallableStatement> extends PreparedStatementWrap<S> implements BasicCallableStatementWrap<S>{
    
    public CallableStatementWrap(S wrapped){
        super(wrapped);
    }
}
