###########################
# written: 03/04/2014
# autor : Melnyk Andriy
###########################

const-flags = -std=c++11 -lpthread
name ?= lab4

all : run

.PHONY : run

run : $(name)
	./$(name)

$(name) : lab4main.cpp CommonResource.h
	g++ -o $(name) $(const-flags) $(flags) lab4main.cpp

.PHONY : clean

clean :
	clear
	rm $(name)