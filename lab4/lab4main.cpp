/************************************************************************
*file: lab4main.cpp
*purpose: lab4 implementation
*author: Melnyk Andriy
*written: 03/04/2014
*last modified: 03/04/2014
*************************************************************************/

#include <iostream>
#include <signal.h>
#include <semaphore.h>
#include <fstream>
#include <unistd.h>

#include "myThread.h"
#include "CommonResource.h"


                        
#define HANDLERS_INIT   signal(SIGINT,&SigIntHandler);  \
                        set_new_handler(&NewHandler);

// #define USLEEP_MODE

#define LOG_WRITE(what) logSync.lock(); log what; logSync.unlock();

#define PRN_TO_LOG(what) LOG_WRITE(<< #what "\n"); what;
                        
using namespace std;
using namespace lab4;

void SigIntHandler(int sig) {
    cerr << endl << "Programm interrupted." << endl;
    exit(EXIT_FAILURE);
}

void NewHandler() {
    cerr << "Heap exhausted. Game over!" << endl;
    exit(EXIT_FAILURE);
}
namespace lab4 {
    ofstream log("lab4.log");  
    MyMutex logSync;
}
sem_t sem1;
sem_t sem2;                  


int main() {
    HANDLERS_INIT;
    sem_init (&sem1, 0 , 0);
    sem_init (&sem2, 0 , 0);
    srand(time(NULL));
    CommonResource<int>* CR1 = new CommonResource<int>(4);
    
    Thread fullSynhronizedP1( [] (void* buffer)->void* {
                                CommonResource<int>* buf = (CommonResource<int>*) buffer;
                                int readEl;
                                while ( !buf->canBreak() ) {
                                    PRN_TO_LOG(sem_post(&sem1));
                                    LOG_WRITE(<< "P1 is waiting for P4\n");
                                    sem_wait(&sem2);
                                    
                                    LOG_WRITE(<< Thread::getCurrentThreadName() << " : Sync was succeeded." << endl);
                                    try {
                                        readEl = buf->read();
                                    } catch (string& e) { 
                                        if ( !buf->canBreak() ) cerr << e << endl; 
                                        break;
                                    }
                                }
                                sem_post(&sem1);
                                LOG_WRITE( << Thread::getCurrentThreadName() << " was stopped.\n");
                             }, CR1 , "P1");
    
    Thread fullSynhronizedP4( [] (void* buffer)->void* {
                                CommonResource<int>* buf = (CommonResource<int>*) buffer;
                                while ( !buf->canBreak() ) {
                                    
                                    PRN_TO_LOG(sem_post(&sem2));
                                    LOG_WRITE( << "P4 is waiting for P1\n");
                                    sem_wait(&sem1);
                                    
                                    LOG_WRITE(<< Thread::getCurrentThreadName() << " : Sync was succeeded." << endl);
                                    
                                    buf->add(rand() % 15);    
                                }
                                sem_post(&sem2);
                                LOG_WRITE(<< Thread::getCurrentThreadName() << " was stopped.\n");
                            }, CR1 , "P4");     
    
    Thread producerP2( [] (void* buffer)->void* {
                            CommonResource<int>* buf = (CommonResource<int>*) buffer;
 
                            while ( !buf->canBreak() ) {
                                buf->add(rand() % 15);
                            }
                            LOG_WRITE( << Thread::getCurrentThreadName() << " was stopped.\n");
                        } , CR1 , "P2");
    
    Thread producerP3( [] (void* buffer)->void* {
                            CommonResource<int>* buf = (CommonResource<int>*) buffer;
                            while (!buf->canBreak()) {
                                buf->add(rand() % 15);
                            }
                            LOG_WRITE(<< Thread::getCurrentThreadName() << " was stopped.\n");
                        } , CR1, "P3");
    
    Thread consumerP5( [] (void* buffer)->void* {
                            CommonResource<int>* buf = (CommonResource<int>*) buffer;
                            int readEl;
                            while ( !buf->canBreak() ) {
                                    try {
                                        readEl = buf->read();
                                    } catch (string& e) { 
                                        if ( !buf->canBreak() ) cerr << e << endl; 
                                        break;
                                    }
                            }
                            LOG_WRITE(<< Thread::getCurrentThreadName() << " was stopped.\n");
                        } , CR1, "P5");
    
    usleep(1000);
    cout << "Life is good!!!" << endl;
    producerP2.join();
    producerP3.join();
    consumerP5.join();
    fullSynhronizedP1.join();
    fullSynhronizedP4.join();
    delete CR1;
    return 0;
}
