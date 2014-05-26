
package lab5;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
    static PrintWriter logF;
    static ThreadGroup lab5threads;
    static PrintWriter UnitTestFile;
    static boolean FatalErrorFlag = false;
    
    public static void main(String[] args) {
        CommonResource1<Integer> comR1 = new CommonResource1<Integer>(4);
        CommonResource2 comR2 = new CommonResource2();
        Semaphore Sem1 = new Semaphore(0);
        Semaphore Sem2 = new Semaphore(0);
        CyclicBarrier CB = new CyclicBarrier(3);
                        
        Producer2 prodP2 = new Producer2("Producer2",comR1,comR2,CB);
        Producer3 prodP3 = new Producer3("Producer3",comR1,Sem1,Sem2);
        Producer4 prodP4 = new Producer4("Producer4",comR1,Sem1,Sem2);
        Consumer1 consP1 = new Consumer1("Consumer1",comR1,comR2,CB);
        Consumer5 consP5 = new Consumer5("Consumer5",comR1,comR2,CB);
        
        try {
            prodP2.join();
            prodP3.join();
            prodP4.join();
            consP1.join();
            consP5.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            lab5threads.interrupt();
            System.exit(1);
        }
        
        if ( FatalErrorFlag | !comR1.canBreak() ) UnitTestFile.println("failed"); 
        else UnitTestFile.println("passed");
        System.out.println("Life is good!");
    }
    
    static {
        try {
            lab5threads = new ThreadGroup("lab5threads");
            logF = new PrintWriter("lab5.log");
            UnitTestFile = new PrintWriter(new BufferedWriter(new FileWriter ("UnitTest.log",true))); 
            Runtime.getRuntime().addShutdownHook(new Thread() {
                                                        public void run() {
                                                                Main.logF.close();
                                                                Main.UnitTestFile.close();
                                                        }
                                                    });
        } catch (IOException e) {
            System.err.println("Error : " + e.getMessage());
            System.exit(1);
        }
    }
        
}

abstract class PraThread extends Thread {
    protected CommonResource1<Integer> BufRes;
    static Random rand = new Random();
    static final int MAX_RAND_EL = 20;
    
    public PraThread(String name,CommonResource1<Integer> buf) {
        super(Main.lab5threads,name);	
        BufRes = buf;
    }
    
    public void run() {
        try {
            insideRun();
        } catch (InterruptedException e) {
            System.err.println("Thread " + this + " was interrupted.");
            Main.lab5threads.interrupt();
            Main.FatalErrorFlag = true;
        }
        Main.logF.println(this + "was stoped.");
    }
    
    abstract protected void insideRun() throws InterruptedException ;
}

abstract class ThreadCR2_CB extends PraThread {
    protected CommonResource2 vars;
    protected CyclicBarrier CB;
    
    public ThreadCR2_CB(String name, CommonResource1<Integer> buf, CommonResource2 vars,CyclicBarrier cb) {
        super(name, buf);
        this.vars = vars;
        CB = cb;
    }
    
    protected void CyclicAwait() throws InterruptedException {
        try {
            Main.logF.println("Before CB.await() in " + this.getName());
            CB.await(1,TimeUnit.SECONDS);
            Main.logF.println("After CB.await() in " + this.getName());
        } catch (TimeoutException e) {
            if ( BufRes.canBreak() ) return; 
            System.err.println("Is buffer full : " + BufRes.isFull() );
            System.err.println("Error in " + this + " : CyclicBarrier await time out." ); 
            throw new InterruptedException();
        } catch (BrokenBarrierException e) {
            if ( BufRes.canBreak() ) return;
            System.err.println("Error in " + this + " : CyclicBarrier was broken." );
            throw new InterruptedException();
        }
    }
        
}

abstract class ThreadSems extends PraThread {
    protected Semaphore sem1;
    protected Semaphore sem2;
    
    public ThreadSems(String name, CommonResource1<Integer> buf,Semaphore sem1,Semaphore sem2) {
        super(name, buf);
        this.sem1 = sem1;
        this.sem2 = sem2;
    }
  
}

class Consumer1 extends ThreadCR2_CB {

    public Consumer1(String name, CommonResource1<Integer> buf,
                    CommonResource2 vars, CyclicBarrier cb) {
        super(name, buf, vars, cb);
        start();
    }

    @Override
    protected void insideRun() throws InterruptedException {
        while( !BufRes.canBreak() ) {			

            BufRes.read();
            CyclicAwait();
            
            try {
                vars.lock();
                Main.logF.println("Mutex was capchured in " + this.getName());
                Main.logF.println("vars before change " + vars);
                vars.d = rand.nextDouble();
                vars.bool = rand.nextBoolean();
                vars.b = (byte) rand.nextInt();
                vars.l = rand.nextLong();
                Main.logF.println("vars after change " + vars);
            } finally {
                vars.unlock();
                Main.logF.println("Mutex was released in " + this.getName());
            }        
        }
    }
}

class Consumer5 extends ThreadCR2_CB {

    public Consumer5(String name, CommonResource1<Integer> buf,
                    CommonResource2 vars, CyclicBarrier cb) {
            super(name, buf, vars, cb);
            start();
    }

    @Override
    protected void insideRun() throws InterruptedException {
        while( !BufRes.canBreak() ) {

            BufRes.read();
            CyclicAwait();
            
            try { 
                vars.lock();
                Main.logF.println("Mutex was capchured in " + this.getName());
                Main.logF.println("vars before change " + vars);
                vars.f = rand.nextFloat();
                vars.d = rand.nextDouble();
                vars.l = rand.nextLong();
                vars.s = (short) rand.nextInt();
                Main.logF.println("vars after change " + vars);
            } finally {
                vars.unlock();
                Main.logF.println("Mutex was released in " + this.getName());
            }
        }
    }
}

class Producer2 extends ThreadCR2_CB {

    public Producer2(String name, CommonResource1<Integer> buf,
                    CommonResource2 vars, CyclicBarrier cb) {
            super(name, buf, vars, cb);
            start();
    }

    @Override
    protected void insideRun() throws InterruptedException {
        while( !BufRes.canBreak() ) { 
            

            do { 
                CyclicAwait();
                synchronized ( BufRes ) {
                    if ( BufRes.isFull() ) continue;
                    BufRes.add(rand.nextInt(MAX_RAND_EL));
                }
           
           } while ( BufRes.isFull() && ! BufRes.canBreak() );
           
            try { 
                vars.lock();
                Main.logF.println("Mutex was capchured in " + this.getName());
                Main.logF.println("vars before change " + vars);
                vars.b = (byte) rand.nextInt();
                vars.c = (char) (65 + Math.abs(rand.nextInt()) % 26);
                vars.bool = rand.nextBoolean();
                vars.i = rand.nextInt();
                Main.logF.println("vars after change " + vars);
            } finally {
                vars.unlock();
                Main.logF.println("Mutex was released in " + this.getName());
            }
        }
    }
}

class Producer3 extends ThreadSems {

    public Producer3(String name, CommonResource1<Integer> buf, Semaphore sem1,
                    Semaphore sem2) {
            super(name, buf, sem1, sem2);
            start();
    }

    @Override
    protected void insideRun() throws InterruptedException {
        while( !BufRes.canBreak() ) {
                
            sem1.release();
            Main.logF.println("sem1 was opened for Producer4 in " + this.getName());
            Main.logF.println(this.getName() + " is waiting for sem2.");
            sem2.acquire();
            Main.logF.println("sem2 was acquired in " + this.getName());
            BufRes.add(rand.nextInt(MAX_RAND_EL));
                                                                                
        }
        sem1.release();
    }
}

class Producer4 extends ThreadSems {

    public Producer4(String name, CommonResource1<Integer> buf, Semaphore sem1,
                    Semaphore sem2) {
        super(name, buf, sem1, sem2);
        start();
    }

    @Override
    protected void insideRun() throws InterruptedException {
        while( !BufRes.canBreak()) {
             
            BufRes.add(rand.nextInt(MAX_RAND_EL));
            
            sem2.release();
            Main.logF.println("sem2 was opened for Producer3 in " + this.getName());
            Main.logF.println(this.getName() + " is waiting for sem1.");
            sem1.acquire();
            Main.logF.println("sem1 was acquired in " + this.getName());
        
        }
        sem2.release();
    }
}
