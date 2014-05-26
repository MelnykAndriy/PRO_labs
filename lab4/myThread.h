/************************************************************************
*file: myThread.h
*purpose: declaration of wrappers on pthread_t , pthread_mutex_t from <pthread.h>
*author: Melnyk Andriy
*written: 03/04/2014
*last modified: 03/04/2014
*************************************************************************/

#ifndef MY_THREAD_HEADER
#define MY_THREAD_HEADER

#include <pthread.h>
#include <map>
#include <sstream>

namespace lab4 {
        using namespace std;
    
        class Lockable {
            public:
                virtual bool tryLock()=0;
                virtual void unlock()=0;
                virtual void lock()=0;
                virtual ~Lockable() { } 
        };

        class LockGuard {
            public:
                LockGuard(Lockable& whatToLock) {
                        locker = &whatToLock;
                        locker->lock();
                }
    
                ~LockGuard() {
                        locker->unlock();
                }
                
                LockGuard() = delete;
                LockGuard(const LockGuard& ) = delete;
                LockGuard operator=(const LockGuard& ) = delete;
            private:
                Lockable* locker;                
        };
        
        class MyMutex : public Lockable {
            public:
                MyMutex(void) {
                    pthread_mutex_init(&Pmutex,NULL);
                }
                
                virtual ~MyMutex(void) {
                    pthread_mutex_destroy(&Pmutex);
                }

                virtual bool tryLock() {
                    return pthread_mutex_trylock(&Pmutex) == 0;
                }

                virtual void unlock() {
                    pthread_mutex_unlock(&Pmutex);
                }

                virtual void lock() {
                    pthread_mutex_lock(&Pmutex);
                }
                
                MyMutex(const MyMutex& ) = delete; 
                MyMutex& operator=(const MyMutex& ) = delete;
            private:
                pthread_mutex_t Pmutex;

        };

        class Thread {
            public:
                typedef void* (*threadFuncT)(void*);
                
                Thread(threadFuncT func, void* param = NULL,string name = string("")) {
                    pthread_create(&innerThread,NULL,func,param);
                    if ( name.length() != 0 ) 
                        threadsIdName[(unsigned long)innerThread] = name;
                    else {
                        ostringstream autoThreadName("thread",ios_base::app);
                        autoThreadName << (unsigned long) innerThread;
                        threadsIdName[(unsigned long)innerThread] = autoThreadName.str();
                    }
                    
                }             
                
                void join() {
                    pthread_join(innerThread,NULL);
                }

                void deatch() {
                    pthread_detach(innerThread);
                }

                unsigned long getId() {
                    return (unsigned long) innerThread;
                }
                
                void setName (string name) {
                    threadsIdName[this->getId()] = name; 
                }
                
                static string getCurrentThreadName() {
                    try {
                        return threadsIdName.at((unsigned long) pthread_self());
                    } catch (out_of_range) {
                        return string("Undefined thread.");
                    }                    
                }
                
                Thread() = delete;
            private:

                static map<unsigned long,string> threadsIdName; 
                
                pthread_t innerThread;
        };
        
       map<unsigned long,string> Thread::threadsIdName;
        
}


#endif