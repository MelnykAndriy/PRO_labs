
package lab5;

public class CommonResource1<T> {
    private T[] buffer;
    private int size;
    private int whereToRead;
    private int whereToWrite;
    private boolean isBufferFull;
    private int readCount;
    
    
    public synchronized T read() throws InterruptedException {
        while (whereToRead == whereToWrite && !isBufferFull) {
                wait(300);
                if ( canBreak() ) return null;
        }
        T retVal = buffer[whereToRead];
        Main.logF.println(retVal + " was readed from buffer by " + Thread.currentThread().getName());
        whereToRead++;
        whereToRead %= size;
        if ( isBufferFull )  {
                isBufferFull = false;
                notify();
        }
        readCount++;
        return retVal;
    }

    public synchronized void add(T el) throws InterruptedException {
        while ( isBufferFull ) {
                wait(300);
                if ( canBreak() ) return ;
        }
        buffer[whereToWrite] = el;
        Main.logF.println(el + " was added to buffer by " + Thread.currentThread().getName());
        whereToWrite++;
        whereToWrite %= size;
        if (whereToWrite == whereToRead) isBufferFull = true;			
        
        notify();
    }
    
    @SuppressWarnings("unchecked")
    public CommonResource1( int size ) {
        buffer = (T[]) new Object[size];
        this.size = size;
        readCount =  whereToWrite = whereToRead = 0;
        isBufferFull = false;
    }
    
    public synchronized boolean isFull() {
        return isBufferFull;
    }
    
    public synchronized boolean isEmpty() {
        return (whereToRead == whereToWrite) && !isBufferFull;
    }
    
    public synchronized boolean canBreak() {
        return readCount >= size*2;
    }
    
    public synchronized void setCanBreak() {
        readCount = size*2;
    }
    
}
