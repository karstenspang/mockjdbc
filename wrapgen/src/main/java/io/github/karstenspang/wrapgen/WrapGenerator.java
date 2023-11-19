package io.github.karstenspang.wrapgen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * Generate wraps around major interfaces in the {@link java.sql} package.<p>
 */
public class WrapGenerator {
    private static final Set<Class<?>> specialInterfaces;
    static{
        specialInterfaces=Set.of(Wrapper.class,AutoCloseable.class);
    }
    private static final Set<Class<?>> interfaces;
    static{
        // Interfaces in java.sql as per JDBC 4.2, except Driver, DriverNotification, and Wrapper.
        interfaces=Set.of(
            java.sql.Array.class,java.sql.Blob.class,java.sql.CallableStatement.class,
            java.sql.Clob.class,java.sql.Connection.class,java.sql.DatabaseMetaData.class,
            java.sql.NClob.class,java.sql.ParameterMetaData.class,
            java.sql.PreparedStatement.class,java.sql.Ref.class,java.sql.ResultSet.class,
            java.sql.ResultSetMetaData.class,java.sql.RowId.class,java.sql.Savepoint.class,
            java.sql.SQLData.class,java.sql.SQLInput.class,java.sql.SQLOutput.class,
            java.sql.SQLType.class,java.sql.SQLXML.class,java.sql.Statement.class,
            java.sql.Struct.class);
    }
    private static final Set<MethodDesc> objectMethods;
    static{
        objectMethods=Arrays.stream(Object.class.getDeclaredMethods()).filter(method->Modifier.isPublic(method.getModifiers())).map(method->new MethodDesc(method)).collect(Collectors.toUnmodifiableSet());
    }
    private static final String basePackageName="io.github.karstenspang.mockjdbc";
    
    /**
     * Generate wraps for the interfaces in java.sql, as per JDBC 4.2 (Java 8).
     * @param baseDir Where to place the output, such as {@code target/generated-classes/java}.
     * @throws IOException if the output cannot be written.
     * @throws ClassNotFoundException if a specified interface does not exist.
     * @throws IllegalArgumentException if one of the found names
     *         extends an interface not found and not {@link Wrapper} or {@link AutoCloseable},
     *         or extends more than one of the found names.
     */
    public static void generateWraps(String baseDir)
        throws IOException,ClassNotFoundException
    {
        Set<Class<?>> knownInterfaces=new HashSet<>(interfaces);
        knownInterfaces.addAll(specialInterfaces);
        File wrapDir=new File(new File(new File(baseDir),basePackageName.replace('.',File.separatorChar)),"wrap");
        File noopDir=new File(new File(new File(baseDir),basePackageName.replace('.',File.separatorChar)),"noop");
        wrapDir.mkdirs();
        noopDir.mkdirs();
        for (Class<?> clazz:interfaces){
            generateWrap(clazz,wrapDir,noopDir,knownInterfaces);
        }
    }
    
    private static void generateWrap(Class<?> ifClass,File wrapDir,File noopDir,Set<Class<?>> knownInterfaces)
        throws IOException
    {
        String wrapPackageName=basePackageName+".wrap";
        String noopPackageName=basePackageName+".noop";
        if (!ifClass.isInterface()) throw new IllegalArgumentException("Class "+ifClass.getName()+" is not an interface");
        if (ifClass.isAnnotation()) throw new IllegalArgumentException("Class "+ifClass.getName()+" is an annotation");
        Set<Class<?>> extendedInterfaces=Set.of(ifClass.getInterfaces());
        if (!knownInterfaces.containsAll(extendedInterfaces)) throw new IllegalArgumentException("Class "+ifClass.getName()+" extends unknown interfaces");
        Set<Class<?>> notSpecialInterfaces=new HashSet<>(extendedInterfaces);
        notSpecialInterfaces.removeAll(specialInterfaces);
        Set<Class<?>> extendedSpecialInterfaces=new HashSet<>(extendedInterfaces);
        extendedSpecialInterfaces.removeAll(notSpecialInterfaces);
        if (notSpecialInterfaces.size()>1) throw new IllegalArgumentException("Class "+ifClass.getName()+" extends more than one interface");
        File wrapJavaFile=new File(wrapDir,ifClass.getSimpleName()+"Wrap.java");
        File noopJavaFile=new File(noopDir,"Noop"+ifClass.getSimpleName()+".java");
        try(OutputStream wos=new FileOutputStream(wrapJavaFile);Writer wrapWriter=new BufferedWriter(new OutputStreamWriter(wos,StandardCharsets.UTF_8));
            OutputStream nos=new FileOutputStream(noopJavaFile);Writer noopWriter=new BufferedWriter(new OutputStreamWriter(nos,StandardCharsets.UTF_8)))
        {
            wrapWriter.write("package "+wrapPackageName+";\n");
            wrapWriter.write("\n");
            wrapWriter.write("import "+basePackageName+".Program;\n");
            wrapWriter.write("import "+basePackageName+".Step;\n");
            wrapWriter.write("import "+basePackageName+".Wrap;\n");
            wrapWriter.write("import "+basePackageName+".Wrapper;\n");
            wrapWriter.write("import "+ifClass.getCanonicalName()+";\n");
            wrapWriter.write("import java.util.Arrays;\n");
            wrapWriter.write("import java.util.function.Supplier;\n");
            wrapWriter.write("import java.util.logging.Logger;\n");
            wrapWriter.write("\n");
            wrapWriter.write("/**\n");
            wrapWriter.write(" * Auto-generated wrap of {@link "+ifClass.getSimpleName()+"} with a {@link Supplier}{@code <}{@link Step}{@code >} (the program).\n");
            wrapWriter.write(" * Every method call will have a step from the program applied, in the order they are returned.\n");
            wrapWriter.write(" */\n");
            wrapWriter.write("public class "+ifClass.getSimpleName()+"Wrap extends "+(notSpecialInterfaces.isEmpty()?"":notSpecialInterfaces.toArray(new Class<?>[1])[0].getSimpleName())+"Wrap implements "+ifClass.getSimpleName()+" {\n");
            wrapWriter.write("    private static final String className=\""+wrapPackageName+"."+ifClass.getSimpleName()+"Wrap\";\n");
            wrapWriter.write("    private static final Logger logger=Logger.getLogger(className);\n");
            wrapWriter.write("    /**\n");
            wrapWriter.write("     * Wrap a {@link "+ifClass.getSimpleName()+"}.\n");
            wrapWriter.write("     * Note that this constructor can be used as a target for {@link Wrapper}{@code <}{@link "+ifClass.getSimpleName()+"}{@code >}.\n");
            wrapWriter.write("     * @param wrapped {@link "+ifClass.getSimpleName()+"} to wrap\n");
            wrapWriter.write("     * @param stepSupplier {@link Supplier}{@code <}{@link Step}{@code >} to wrap the object with\n");
            wrapWriter.write("     */\n");
            wrapWriter.write("    public "+ifClass.getSimpleName()+"Wrap("+ifClass.getSimpleName()+" wrapped,Supplier<Step> stepSupplier){\n");
            wrapWriter.write("        this(className,wrapped,stepSupplier);\n");
            wrapWriter.write("    }\n");
            wrapWriter.write("    /**\n");
            wrapWriter.write("     * Wrap a {@link "+ifClass.getSimpleName()+"}.\n");
            wrapWriter.write("     * Convenience constructor that uses a {@link Program} to provide the steps.\n");
            wrapWriter.write("     * @param wrapped {@link "+ifClass.getSimpleName()+"} to wrap\n");
            wrapWriter.write("     * @param steps {@link Step}s to wrap the object with\n");
            wrapWriter.write("     */\n");
            wrapWriter.write("    public "+ifClass.getSimpleName()+"Wrap("+ifClass.getSimpleName()+" wrapped,Iterable<Step> steps){\n");
            wrapWriter.write("        this(wrapped,new Program(steps));\n");
            wrapWriter.write("    }\n");
            wrapWriter.write("    protected "+ifClass.getSimpleName()+"Wrap(String className,"+ifClass.getSimpleName()+" wrapped,Supplier<Step> stepSupplier){\n");
            wrapWriter.write("        super(className,wrapped,stepSupplier);\n");
            wrapWriter.write("    }\n");
            wrapWriter.write("\n");
            
            noopWriter.write("package "+noopPackageName+";\n");
            noopWriter.write("\n");
            noopWriter.write("import "+ifClass.getCanonicalName()+";\n");
            noopWriter.write("\n");
            noopWriter.write("/**\n");
            noopWriter.write(" * No-op implementaion of {@link "+ifClass.getSimpleName()+"}.\n");
            noopWriter.write(" */\n");
            noopWriter.write("public class Noop"+ifClass.getSimpleName());
            if (!notSpecialInterfaces.isEmpty()){
                noopWriter.write(" extends Noop"+notSpecialInterfaces.toArray(new Class<?>[1])[0].getSimpleName());
            }
            noopWriter.write(" implements "+ifClass.getSimpleName()+" {\n");
            noopWriter.write("    private static Noop"+ifClass.getSimpleName()+" instance=new Noop"+ifClass.getSimpleName()+"();\n");
            noopWriter.write("    /**\n");
            noopWriter.write("     * Access to the instance.\n");
            noopWriter.write("     * @return the only instance of {@link Noop"+ifClass.getSimpleName()+"}\n");
            noopWriter.write("     */\n");
            noopWriter.write("    public static Noop"+ifClass.getSimpleName()+" instance(){return instance;}\n");
            noopWriter.write("    protected Noop"+ifClass.getSimpleName()+"(){}\n");
            Method[] methods=ifClass.getDeclaredMethods();
            if (methods.length>0||extendedSpecialInterfaces.contains(Wrapper.class)){
                wrapWriter.write("    @SuppressWarnings(\"unchecked\")\n");
                wrapWriter.write("    private final "+ifClass.getSimpleName()+" getWrapped"+ifClass.getSimpleName()+"(){\n");
                wrapWriter.write("        return ("+ifClass.getSimpleName()+")wrapped;\n");
                wrapWriter.write("    }\n");
                wrapWriter.write("\n");
            }
            if (extendedSpecialInterfaces.contains(Wrapper.class)){
                wrapWriter.write("    @Override\n");
                wrapWriter.write("    public boolean isWrapperFor​(Class<?> iface)\n");
                wrapWriter.write("        throws java.sql.SQLException\n");
                wrapWriter.write("    {\n");
                wrapWriter.write("        Step step=stepSupplier.get();\n");
                wrapWriter.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+".isWrapperFor​(\"+String.valueOf(iface)+\")\");\n");
                wrapWriter.write("        boolean result=step.apply(()->getWrapped"+ifClass.getSimpleName()+"().isWrapperFor​(iface));\n");
                wrapWriter.write("        logger.finest(\"Result: \"+String.valueOf(result));\n");
                wrapWriter.write("        return result;\n");
                wrapWriter.write("    }\n");
                wrapWriter.write("    @Override\n");
                wrapWriter.write("    public <T> T unwrap​(Class<T> iface)\n");
                wrapWriter.write("        throws java.sql.SQLException\n");
                wrapWriter.write("    {\n");
                wrapWriter.write("        Step step=stepSupplier.get();\n");
                wrapWriter.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+".unWrap(\"+String.valueOf(iface)+\")\");\n");
                wrapWriter.write("        T result=stepSupplier.get().apply(()->getWrapped"+ifClass.getSimpleName()+"().unwrap(iface));\n");
                wrapWriter.write("        logger.finest(\"Result: \"+String.valueOf(result));\n");
                wrapWriter.write("        return result;\n");
                wrapWriter.write("    }\n");
            }
            if (getAllInterfaces(ifClass).contains(Wrapper.class)){
                noopWriter.write("    /**\n");
                noopWriter.write("     * Checks whether the the specified class or interface is a superclass of {@link Noop"+ifClass.getSimpleName()+"}.\n");
                noopWriter.write("     * @param iface Class or interface to check\n");
                noopWriter.write("     * @return {@code true} if {@code iface} is a superclass of\n");
                noopWriter.write("     *         {@link Noop"+ifClass.getSimpleName()+"}, otherwise {@code false}.\n");
                noopWriter.write("     */\n");
                noopWriter.write("    @Override\n");
                noopWriter.write("    public boolean isWrapperFor​(Class<?> iface)\n");
                noopWriter.write("    {\n");
                noopWriter.write("        try{\n");
                noopWriter.write("            Noop"+ifClass.getSimpleName()+".class.asSubclass(iface);\n");
                noopWriter.write("            return true;\n");
                noopWriter.write("        }\n");
                noopWriter.write("        catch(ClassCastException e){\n");
                noopWriter.write("            return false;\n");
                noopWriter.write("        }\n");
                noopWriter.write("    }\n");
                noopWriter.write("    /**\n");
                noopWriter.write("     * Casts {@code this} to {@code T}\n");
                noopWriter.write("     * @param <T> class to cast to\n");
                noopWriter.write("     * @param iface {@link Class} to cast to\n");
                noopWriter.write("     * @return {@code (T)this}\n");
                noopWriter.write("     * @throws java.sql.SQLException if {@code T} is not a superclass of {@link Noop"+ifClass.getSimpleName()+"}.\n");
                noopWriter.write("     *         The cause is a {@link ClassCastException}.\n");
                noopWriter.write("     */\n");
                noopWriter.write("    @Override\n");
                noopWriter.write("    public <T> T unwrap​(Class<T> iface)\n");
                noopWriter.write("        throws java.sql.SQLException\n");
                noopWriter.write("    {\n");
                noopWriter.write("        try{\n");
                noopWriter.write("            return iface.cast(this);\n");
                noopWriter.write("        }\n");
                noopWriter.write("        catch(ClassCastException e){\n");
                noopWriter.write("            throw new java.sql.SQLException(e);\n");
                noopWriter.write("        }\n");
                noopWriter.write("    }\n");
            }
            for (Method method:methods){
                int modifiers=method.getModifiers();
                if (Modifier.isStatic(modifiers)) continue;
                if (!Modifier.isPublic(modifiers)) continue;
                if (objectMethods.contains(new MethodDesc(method))) continue;
                writeMethodHeader(wrapWriter,method);
                writeMethodHeader(noopWriter,method);
                
                Set<Class<?>> exceptions=Set.of(method.getExceptionTypes());
                if (!exceptions.isEmpty()){
                    wrapWriter.write("        throws ");
                    int i=0;
                    for (Class<?> exception:exceptions){
                        if (i!=0) wrapWriter.write(",");
                        wrapWriter.write(exception.getCanonicalName());
                        i++;
                    }
                    wrapWriter.write("\n");
                }
                wrapWriter.write("    {\n");
                if (!exceptions.contains(SQLException.class)){
                    wrapWriter.write("        try{\n");
                }
                wrapWriter.write("        Step step=stepSupplier.get();\n");
                wrapWriter.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+"."+method.getName()+"(\"");
                int pno=0;
                for (Class<?> param:method.getParameterTypes()){
                    if (pno!=0){
                        wrapWriter.write("+\",\"");
                    }
                    if (param.isArray()){
                        wrapWriter.write("+Arrays.toString(p"+String.valueOf(pno)+")");
                    }
                    else{
                        wrapWriter.write("+String.valueOf(p"+String.valueOf(pno)+")");
                    }
                    pno++;
                }
                wrapWriter.write("+\")\");\n");
                wrapWriter.write("        ");
                if (!"void".equals(method.getGenericReturnType().getTypeName())){
                    wrapWriter.write(method.getGenericReturnType().getTypeName()+" result=");
                }
                wrapWriter.write("step.apply(()->getWrapped"+ifClass.getSimpleName()+"()."+method.getName()+"(");
                for (int a=0;a<method.getParameterCount();a++){
                    if (a!=0){
                        wrapWriter.write(",");
                    }
                    wrapWriter.write("p"+String.valueOf(a));
                }
                wrapWriter.write("));\n");
                if (!"void".equals(method.getGenericReturnType().getTypeName())){
                    wrapWriter.write("        logger.finest(\"Result: \"+");
                    if (method.getReturnType().isArray()){
                        wrapWriter.write("Arrays.toString(result)");
                    }
                    else{
                        wrapWriter.write("String.valueOf(result)");
                    }
                    wrapWriter.write(");\n");
                    wrapWriter.write("        return result;\n");
                }
                if (!exceptions.contains(SQLException.class)){
                    wrapWriter.write("        }\n");
                    if (!exceptions.isEmpty()){
                        wrapWriter.write("        catch(");
                        int i=0;
                        for (Class<?> exception:exceptions){
                            if (i!=0) wrapWriter.write("|");
                            wrapWriter.write(exception.getCanonicalName());
                            i++;
                        }
                        wrapWriter.write(" e){\n");
                        wrapWriter.write("            throw e;\n");
                        wrapWriter.write("        }\n");
                    }
                    wrapWriter.write("        catch(java.sql.SQLException e){\n");
                    wrapWriter.write("            throw new UnsupportedOperationException(\"unsupported exception\",e);\n");
                    wrapWriter.write("        }\n");
                }
                wrapWriter.write("    }\n");
                
                noopWriter.write("    {\n");
                Class<?> returnType=method.getReturnType();
                if (returnType==Void.TYPE){
                    // Nothing
                }
                else if (interfaces.contains(returnType)){
                    noopWriter.write("        return Noop"+returnType.getSimpleName()+".instance();\n");
                }
                else if (returnType==Integer.TYPE){
                    noopWriter.write("        return 0;\n");
                }
                else if (returnType==Long.TYPE){
                    noopWriter.write("        return 0L;\n");
                }
                else if (returnType==Short.TYPE){
                    noopWriter.write("        return 0;\n");
                }
                else if (returnType==Byte.TYPE){
                    noopWriter.write("        return 0;\n");
                }
                else if (returnType==Double.TYPE){
                    noopWriter.write("        return 0D;\n");
                }
                else if (returnType==Float.TYPE){
                    noopWriter.write("        return 0F;\n");
                }
                else if (returnType==Boolean.TYPE){
                    if (method.getName().equals("wasNull")){
                        noopWriter.write("        return true;\n");
                    }
                    else{
                        noopWriter.write("        return false;\n");
                    }
                }
                else{
                    noopWriter.write("        return null;\n");
                }
                noopWriter.write("    }\n");
            }
            noopWriter.write("    /** @return {@code \"Noop"+ifClass.getSimpleName()+"\"} */\n");
            noopWriter.write("    @Override\n");
            noopWriter.write("    public String toString(){\n");
            noopWriter.write("        return \"Noop"+ifClass.getSimpleName()+"\";\n");
            noopWriter.write("    }\n");
            
            wrapWriter.write("}\n");
            noopWriter.write("}\n");
        }
    }
    
    private static void writeMethodHeader(Writer writer,Method method)
        throws IOException
    {
        if (method.getAnnotation(Deprecated.class)!=null){
            writer.write("    @Deprecated\n");
        }
        writer.write("    @Override\n");
        writer.write("    public ");
        TypeVariable<Method>[] typeParameters=method.getTypeParameters();
        if (typeParameters.length!=0){
            writer.write("<");
            int x=0;
            for (TypeVariable<Method> typeParameter:typeParameters){
                if (x!=0) writer.write(",");
                writer.write(typeParameter.getName());
                Type[] bounds=typeParameter.getBounds();
                if (bounds.length!=0){
                    writer.write(" extends ");
                    int y=0;
                    for (Type bound:bounds){
                        if (y!=0) writer.write("&");
                        writer.write(bound.getTypeName());
                    }
                }
            }
            writer.write("> ");
        }
        writer.write(method.getGenericReturnType().getTypeName()+" "+method.getName()+"(");
        int pno=0;
        for (Type param:method.getGenericParameterTypes()){
            if (pno!=0){
                writer.write(",");
            }
            writer.write(param.getTypeName()+" p"+String.valueOf(pno));
            pno++;
        }
        writer.write(")\n");
    }
    
    private static Set<Class<?>> getAllInterfaces(Class<?> type) {
        return Stream.of(type.getInterfaces())
                     .flatMap(interfaceType -> Stream.concat(Stream.of(interfaceType), getAllInterfaces(interfaceType).stream()))
                     .collect(Collectors.toSet());
    }
}
