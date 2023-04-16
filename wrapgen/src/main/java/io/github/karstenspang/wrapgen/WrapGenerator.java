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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Generate wraps around major interfaces in the {@link java.sql} package.<p>
 * To do: Handle bounds of type parameters of methods.
 */
public class WrapGenerator {
    private static final Set<Class<?>> specialInterfaces;
    static{
        specialInterfaces=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Wrapper.class,AutoCloseable.class)));
    }
    
    /**
     * Generate wraps for a number of interfaces.
     * @param ifNames Simple names of interfaces
     * @param packageName Name of the package of the generated classes. This also determines the subdirectory to place the output.
     * @param baseDir Where to place the output, such as {@code target/generated-classes/java}.
     * @throws IOException if the output cannot be written.
     * @throws ClassNotFoundException if a specified interface does not exist.
     * @throws IllegalArgumentException if one of the specified names is not an interface,
     *         is an annotation,
     *         extends an interface not specified and not {@link Wrapper} or {@link AutoCloseable},
     *         or extends more than one of the specified names.
     */
    public static void generateWraps(Iterable<String> ifNames,String packageName,String baseDir)
        throws IOException,ClassNotFoundException
    {
        Set<Class<?>> interfaces=new HashSet<>();
        for (String ifName:ifNames){
            interfaces.add(Class.forName("java.sql."+ifName));
        }
        Set<Class<?>> knownInterfaces=new HashSet<>(interfaces);
        knownInterfaces.addAll(specialInterfaces);
        File dir=new File(new File(new File(baseDir),packageName.replace('.',File.separatorChar)),"wrap");
        dir.mkdirs();
        for (Class<?> clazz:interfaces){
            generateWrap(clazz,packageName,dir,knownInterfaces);
        }
    }
    
    private static void generateWrap(Class<?> ifClass,String basePackageName,File dir,Set<Class<?>> knownInterfaces)
        throws IOException
    {
        String packageName=basePackageName+".wrap";
        if (!ifClass.isInterface()) throw new IllegalArgumentException("Class "+ifClass.getName()+" is not an interface");
        if (ifClass.isAnnotation()) throw new IllegalArgumentException("Class "+ifClass.getName()+" is an annotation");
        Set<Class<?>> extendedInterfaces=new HashSet<>(Arrays.asList(ifClass.getInterfaces()));
        if (!knownInterfaces.containsAll(extendedInterfaces)) throw new IllegalArgumentException("Class "+ifClass.getName()+" extends unknown interfaces");
        Set<Class<?>> notSpecialInterfaces=new HashSet<>(extendedInterfaces);
        notSpecialInterfaces.removeAll(specialInterfaces);
        Set<Class<?>> extendedSpecialInterfaces=new HashSet<>(extendedInterfaces);
        extendedSpecialInterfaces.removeAll(notSpecialInterfaces);
        if (notSpecialInterfaces.size()>1) throw new IllegalArgumentException("Class "+ifClass.getName()+" extends more than one interface");
        File javaFile=new File(dir,ifClass.getSimpleName()+"Wrap.java");
        try(OutputStream os=new FileOutputStream(javaFile);Writer writer=new BufferedWriter(new OutputStreamWriter(os,StandardCharsets.UTF_8)))
        {
            writer.write("package "+packageName+";\n");
            writer.write("\n");
            writer.write("import "+basePackageName+".Program;\n");
            writer.write("import "+basePackageName+".Step;\n");
            writer.write("import "+basePackageName+".Wrap;\n");
            writer.write("import "+basePackageName+".Wrapper;\n");
            writer.write("import "+ifClass.getCanonicalName()+";\n");
            writer.write("import java.util.Arrays;\n");
            writer.write("import java.util.logging.Logger;\n");
            writer.write("\n");
            writer.write("/**\n");
            writer.write(" * Auto-generated wrap of {@link "+ifClass.getSimpleName()+"} with a {@link Program}.\n");
            writer.write(" * Every method call will have a step from the program applied, in order.\n");
            writer.write(" */\n");
            writer.write("public class "+ifClass.getSimpleName()+"Wrap extends "+(notSpecialInterfaces.isEmpty()?"":notSpecialInterfaces.toArray(new Class<?>[1])[0].getSimpleName())+"Wrap implements "+ifClass.getSimpleName()+" {\n");
            writer.write("    private static Logger logger=Logger.getLogger(\""+packageName+"."+ifClass.getSimpleName()+"Wrap\");\n");
            writer.write("    /**\n");
            writer.write("     * Wrap a {@link "+ifClass.getSimpleName()+"}.\n");
            writer.write("     * Note that this constructor can be used as a target for {@link Wrapper}<code>&lt;</code>{@link "+ifClass.getSimpleName()+"}<code>&gt;</code>.\n");
            writer.write("     * @param wrapped {@link "+ifClass.getSimpleName()+"} to wrap\n");
            writer.write("     * @param program {@link Program} to wrap the object with\n");
            writer.write("     */\n");
            writer.write("     public "+ifClass.getSimpleName()+"Wrap("+ifClass.getSimpleName()+" wrapped,Program program){\n");
            writer.write("         this("+ifClass.getSimpleName()+"Wrap.class,wrapped,program);\n");
            writer.write("     }\n");
            writer.write("     protected "+ifClass.getSimpleName()+"Wrap(Class<? extends "+ifClass.getSimpleName()+"Wrap> clazz,"+ifClass.getSimpleName()+" wrapped,Program program){\n");
            writer.write("         super(clazz,wrapped,program);\n");
            writer.write("     }\n");
            writer.write("\n");
            writer.write("     @SuppressWarnings(\"unchecked\")\n");
            writer.write("     private final "+ifClass.getSimpleName()+" getWrapped"+ifClass.getSimpleName()+"(){\n");
            writer.write("         return ("+ifClass.getSimpleName()+")wrapped;\n");
            writer.write("     }\n");
            writer.write("\n");
            if (extendedSpecialInterfaces.contains(Wrapper.class)){
                writer.write("    @Override\n");
                writer.write("    public boolean isWrapperFor​(Class<?> iface)\n");
                writer.write("        throws java.sql.SQLException\n");
                writer.write("    {\n");
                writer.write("        Step step=steps.next();\n");
                writer.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+".isWrapperFor​(\"+String.valueOf(iface)+\")\");\n");
                writer.write("        return step.apply(()->getWrapped"+ifClass.getSimpleName()+"().isWrapperFor​(iface));\n");
                writer.write("    }\n");
                writer.write("    @Override\n");
                writer.write("    public <T> T unwrap​(Class<T> iface)\n");
                writer.write("        throws java.sql.SQLException\n");
                writer.write("    {\n");
                writer.write("        Step step=steps.next();\n");
                writer.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+".unWrap(\"+String.valueOf(iface)+\")\");\n");
                writer.write("        return steps.next().apply(()->getWrapped"+ifClass.getSimpleName()+"().unwrap(iface));\n");
                writer.write("    }\n");
            }
            for (Method method:ifClass.getDeclaredMethods()){
                int modifiers=method.getModifiers();
                if (Modifier.isStatic(modifiers)) continue;
                if (!Modifier.isPublic(modifiers)) continue;
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
                Set<Class<?>> exceptions=new HashSet<>(Arrays.asList(method.getExceptionTypes()));
                if (!exceptions.isEmpty()){
                    writer.write("        throws ");
                    int i=0;
                    for (Class<?> exception:exceptions){
                        if (i!=0) writer.write(",");
                        writer.write(exception.getCanonicalName());
                        i++;
                    }
                    writer.write("\n");
                }
                writer.write("    {\n");
                if (!exceptions.isEmpty() && !exceptions.contains(SQLException.class)){
                    writer.write("        try{\n");
                }
                writer.write("        Step step=steps.next();\n");
                writer.write("        logger.finest(\"Apply \"+String.valueOf(step)+\" to "+ifClass.getSimpleName()+"."+method.getName()+"(\"");
                pno=0;
                for (Class<?> param:method.getParameterTypes()){
                    if (pno!=0){
                        writer.write("+\",\"");
                    }
                    if (param.isArray()){
                        writer.write("+Arrays.toString(p"+String.valueOf(pno)+")");
                    }
                    else{
                        writer.write("+String.valueOf(p"+String.valueOf(pno)+")");
                    }
                    pno++;
                }
                writer.write("+\")\");\n");
                writer.write("        ");
                if (!"void".equals(method.getGenericReturnType().getTypeName())){
                    writer.write("return ");
                }
                writer.write("step.apply(()->getWrapped"+ifClass.getSimpleName()+"()."+method.getName()+"(");
                for (int a=0;a<method.getParameterCount();a++){
                    if (a!=0){
                        writer.write(",");
                    }
                    writer.write("p"+String.valueOf(a));
                }
                writer.write("));\n");
                if (!exceptions.isEmpty() && !exceptions.contains(SQLException.class)){
                    writer.write("        }\n");
                    writer.write("        catch(");
                    int i=0;
                    for (Class<?> exception:exceptions){
                        if (i!=0) writer.write("|");
                        writer.write(exception.getCanonicalName());
                        i++;
                    }
                    writer.write(" e){\n");
                    writer.write("            throw e;\n");
                    writer.write("        }\n");
                    writer.write("        catch(java.sql.SQLException e){\n");
                    writer.write("            throw new IllegalStateException(\"unexpected exception\",e);\n");
                    writer.write("        }\n");
                }
                writer.write("    }\n");
            }
            writer.write("}\n");
        }
    }
    
    public static void main(String[] args)
        throws IOException,ClassNotFoundException
    {
        generateWraps(Arrays.asList("Connection","Statement","CallableStatement","PreparedStatement"),"io.github.karstenspang.mockjdbc","generated-sources/java");
    }
}
