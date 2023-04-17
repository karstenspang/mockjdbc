package io.github.karstenspang.wrapgen;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MethodDesc {
    private Method method;
    public MethodDesc(Method method){
        this.method=method;
    }
    @Override
    public boolean equals(Object other){
        if (other==this) return true;
        if (!(other instanceof MethodDesc)) return false;
        MethodDesc that=(MethodDesc) other;
        if (this.method==that.method) return true;
        if (this.method==null||that.method==null) return false;
        return Objects.equals(this.method.getName(),that.method.getName()) &&
               Arrays.equals(this.method.getParameterTypes(),that.method.getParameterTypes());
    }
    @Override
    public int hashCode(){
        if (method==null) return 0;
        return Arrays.deepHashCode(new Object[]{method.getName(),method.getParameterTypes()});
    }
}
