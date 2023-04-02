package io.github.karstenspang.mockjdbc;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class PreparedStatementWrap<S extends PreparedStatement> extends StatementWrap<S> implements BasicPreparedStatementWrap<S>{
    
    public PreparedStatementWrap(S wrapped){
        super(wrapped);
    }
}
