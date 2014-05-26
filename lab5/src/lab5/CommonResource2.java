
package lab5;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommonResource2 {
    private Lock locker = new ReentrantLock(true);
    public byte b;
    public short s;
    public int i;
    public long l;
    public char c;
    public float f;
    public double d;
    public boolean bool;
    
    public CommonResource2() {
        Random rand = new Random();
        b = (byte) rand.nextInt();
        s = (short) rand.nextInt();
        i = rand.nextInt();
        l = rand.nextLong();
        c = (char) (65 + Math.abs(rand.nextInt()) % 26);
        f = rand.nextFloat();
        d = rand.nextDouble();
        bool = rand.nextBoolean();
    }
    
    public void lock() {
        locker.lock();
    }
    
    public void unlock() {
        locker.unlock();
    }
    
    public String toString() {
        return  " b : " + b +
                " s : " + s +
                " i : " + i + 
                " l : " + l +
                " c : " + c +
                " f : " + f +
                " d : " + d +
                " bool : " + bool;
    }
}