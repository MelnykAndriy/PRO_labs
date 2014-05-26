/************************************************************************
*file: CommonResource.h
*purpose: declaration of class CommonResource
*author: Melnyk Andriy
*written: 03/04/2014
*last modified: 03/04/2014
*************************************************************************/

#ifndef COMMON_RESOURCE_HEADER
#define COMMON_RESOURCE_HEADER

#define LOG_WRITE(what) logSync.lock(); log what; logSync.unlock();

#include "myThread.h"
#include <stdexcept>
#include <semaphore.h>

namespace lab4 {

extern ofstream log;
extern MyMutex logSync;
    
template <typename T> class CommonResource {
    public:

        CommonResource(int size) throw (std::invalid_argument);
        ~CommonResource(void) {
            delete buf;
        }

        void add(const T& );
        T read();
        
        unsigned int getElsInBuffer() const { LockGuard locker(mutex); return size; }
        
        bool canBreak() {
            LockGuard locker(mutex);
            return summaryReadedEl >= size*2 ;
        }
        
        bool isFull() {
            return size == elsInQueue;
        }
        
        bool isEmpty() {
            return 0 == elsInQueue;
        }
        
    private:
        T* buf;
        const unsigned int size;
        unsigned int whereToRead;
        unsigned int whereToWrite;
        unsigned int elsInQueue;
        unsigned int summaryReadedEl;
        MyMutex mutex;
        sem_t queue;

};

template <typename T> CommonResource<T>::CommonResource(int size) throw (std::invalid_argument)  : size(size){
    if (size <= 0) throw std::invalid_argument("Size of buffer should be positive.");
    buf = new T[size];
    elsInQueue = whereToRead = whereToWrite = 0;
    sem_init(&queue,0,size);
}

template <typename T> void CommonResource<T>::add(const T& addEl) {
    LockGuard locker(mutex);
    
    if ( sem_trywait(&queue) == 0 ) {
    
        buf[whereToWrite] = addEl;
 
        LOG_WRITE(<< addEl << " was added to buffer by " << Thread::getCurrentThreadName() << endl);

        whereToWrite++;
        whereToWrite %= size;
        elsInQueue++;
    } 
}

template <typename T> T CommonResource<T>::read() {
    T retVal;
    
    LockGuard locker(mutex);
    
    for (int i = 0 ; elsInQueue == 0 ; i++ ) {
        mutex.unlock();
        usleep(30000);
        if (i == 5 ) throw string("Buffer is empty.");
        mutex.lock();
    }
        
    retVal = buf[whereToRead];

    LOG_WRITE(<< retVal << " was readed from buffer by " << Thread::getCurrentThreadName() << endl);

    whereToRead++;
    whereToRead %= size;
    elsInQueue--;
    sem_post(&queue);
    summaryReadedEl++;
    return retVal;
}


}

#endif