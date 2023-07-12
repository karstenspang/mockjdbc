package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Supplier of steps that wraps every result with this supplier.
 * Specifically, the steps act like a {@link WrapperStep} if the result
 * has a known {@link Wrapper}, and a {@link PassThruStep} otherwise.
 * The created wraps have this supplier as their program. The result is that
 * every JDBC method call will go through a wrap. The effect of this is
 * that all calls will be logged with level {@link Level#FINEST}, corresponding
 * to {@code TRACE} in most logging backends. If you configure your backend to
 * log {@code io.github.karstenspang.mockjdbc} at trace level, you will get a
 * complete log of all JDBC calls, including arguments and returned results.
 */
public class RecursiveWrapperStepSupplier implements Supplier<Step> {
    private static final RecursiveWrapperStepSupplier instance=new RecursiveWrapperStepSupplier();
    private static final RecursiveWrapperStep stepInstance=new RecursiveWrapperStep();
    
    /**
     * Get the instance
     * @return (the only) instance
     */
    public static RecursiveWrapperStepSupplier instance(){return instance;}
    
    /**
     * Get the step
     * @return (the only) step instance
     */
    @Override
    public Step get(){return stepInstance;}
    
    /**
     * Get the string representation
     * @return the string representation
     */
    @Override
    public String toString(){return "RecursiveWrapperStepSupplier";}
    
    // Important that CallableStatement, PreparedStatement, and Statement come in that order. The same applies to NClob and Clob.
    private static final ClassWrapper<?>[] classWrappers={
        new ClassWrapper<java.sql.Array            >(java.sql.Array.class            ,io.github.karstenspang.mockjdbc.wrap.ArrayWrap::new            ),
        new ClassWrapper<java.sql.Blob             >(java.sql.Blob.class             ,io.github.karstenspang.mockjdbc.wrap.BlobWrap::new             ),
        new ClassWrapper<java.sql.CallableStatement>(java.sql.CallableStatement.class,io.github.karstenspang.mockjdbc.wrap.CallableStatementWrap::new),
        new ClassWrapper<java.sql.Connection       >(java.sql.Connection.class       ,io.github.karstenspang.mockjdbc.wrap.ConnectionWrap::new       ),
        new ClassWrapper<java.sql.DatabaseMetaData >(java.sql.DatabaseMetaData.class ,io.github.karstenspang.mockjdbc.wrap.DatabaseMetaDataWrap::new ),
        new ClassWrapper<java.sql.NClob            >(java.sql.NClob.class            ,io.github.karstenspang.mockjdbc.wrap.NClobWrap::new            ),
        new ClassWrapper<java.sql.Clob             >(java.sql.Clob.class             ,io.github.karstenspang.mockjdbc.wrap.ClobWrap::new             ),
        new ClassWrapper<java.sql.ParameterMetaData>(java.sql.ParameterMetaData.class,io.github.karstenspang.mockjdbc.wrap.ParameterMetaDataWrap::new),
        new ClassWrapper<java.sql.PreparedStatement>(java.sql.PreparedStatement.class,io.github.karstenspang.mockjdbc.wrap.PreparedStatementWrap::new),
        new ClassWrapper<java.sql.Ref              >(java.sql.Ref.class              ,io.github.karstenspang.mockjdbc.wrap.RefWrap::new              ),
        new ClassWrapper<java.sql.ResultSet        >(java.sql.ResultSet.class        ,io.github.karstenspang.mockjdbc.wrap.ResultSetWrap::new        ),
        new ClassWrapper<java.sql.ResultSetMetaData>(java.sql.ResultSetMetaData.class,io.github.karstenspang.mockjdbc.wrap.ResultSetMetaDataWrap::new),
        new ClassWrapper<java.sql.RowId            >(java.sql.RowId.class            ,io.github.karstenspang.mockjdbc.wrap.RowIdWrap::new            ),
        new ClassWrapper<java.sql.Savepoint        >(java.sql.Savepoint.class        ,io.github.karstenspang.mockjdbc.wrap.SavepointWrap::new        ),
        new ClassWrapper<java.sql.SQLData          >(java.sql.SQLData.class          ,io.github.karstenspang.mockjdbc.wrap.SQLDataWrap::new          ),
        new ClassWrapper<java.sql.SQLInput         >(java.sql.SQLInput.class         ,io.github.karstenspang.mockjdbc.wrap.SQLInputWrap::new         ),
        new ClassWrapper<java.sql.SQLOutput        >(java.sql.SQLOutput.class        ,io.github.karstenspang.mockjdbc.wrap.SQLOutputWrap::new        ),
        new ClassWrapper<java.sql.SQLType          >(java.sql.SQLType.class          ,io.github.karstenspang.mockjdbc.wrap.SQLTypeWrap::new          ),
        new ClassWrapper<java.sql.SQLXML           >(java.sql.SQLXML.class           ,io.github.karstenspang.mockjdbc.wrap.SQLXMLWrap::new           ),
        new ClassWrapper<java.sql.Statement        >(java.sql.Statement.class        ,io.github.karstenspang.mockjdbc.wrap.StatementWrap::new        ),
        new ClassWrapper<java.sql.Struct           >(java.sql.Struct.class           ,io.github.karstenspang.mockjdbc.wrap.StructWrap::new           )
    };
    
    private static class ClassWrapper<T> {
        private final Class<T> clazz;
        private final Wrapper<T> wrapper;
        public ClassWrapper(Class<T> clazz,Wrapper<T> wrapper)
        {
            this.clazz=clazz;
            this.wrapper=wrapper;
        }
        public T wrapIfMatch(Object o){
            T wrapped;
            try{
                wrapped=clazz.cast(o);
            }
            catch(ClassCastException e){
                return null;
            }
            return wrapper.wrap(wrapped,instance);
        }
    }
    
    private static class RecursiveWrapperStep extends PassThruStep {
        @Override
        public <T> T apply(SQLSupplier<? extends T> supplier)
            throws SQLException
        {
            T result=supplier.get();
            for (ClassWrapper<?> classWrapper:classWrappers){
                @SuppressWarnings("unchecked")
                T wrap=(T)classWrapper.wrapIfMatch(result);
                if (wrap!=null) return wrap;
            }
            return result;
        }
        @Override
        public String toString(){return "RecursiveWrapperStep";}
    }
    
    private RecursiveWrapperStepSupplier(){}
}
