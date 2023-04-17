package io.github.karstenspang.mockjdbc;

import org.opentest4j.AssertionFailedError;

public class TestThread extends Thread {
    public interface Executable {
        public void execute() throws Exception;
    }
    
    public TestThread(Executable exe){
        ex=null;
        this.exe=exe;
    }
    private Throwable ex;
    private final Executable exe;
    @Override
    public void run(){
        try{
            exe.execute();
        }
        catch(Exception|AssertionFailedError e){
            ex=e;
        }
    }
    
    public void joinAndCheck()
        throws Exception
    {
        join();
        if (ex==null) return;
        if (ex instanceof Exception) throw (Exception) ex;
        if (ex instanceof AssertionFailedError) throw (AssertionFailedError)ex;
        throw new IllegalStateException("How did this happen?",ex);
    }
}