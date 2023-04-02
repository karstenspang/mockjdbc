package io.github.karstenspang.mockjdbc;

import java.sql.Statement;
import java.sql.SQLException;

public class StatementWrap<S extends Statement> implements BasicStatementWrap<S>{
    private S wrapped;
    
    public StatementWrap(S wrapped){
        this.wrapped=wrapped;
    }
    
    public StatementWrap<Statement> create(Statement wrapped){
        return new StatementWrap<>(wrapped);
    }
    
    @Override
    public S wrapped(){return wrapped;}
}
