########################## 
# written: 07/04/2014
# autor : Melnyk Andriy
##########################

name ?=lab5
packages ?=$(name)
sourcepath ?=$(shell pwd)/src
classpath ?=$(shell pwd)/bin
main ?=Main
manifestline =Main-Class: $(packages).$(main)

all : run

.PHONY : run

run : $(name).jar
	java -jar $(name).jar

$(name).jar : $(shell pwd)/src/*
	javac -d $(classpath) -sourcepath $(sourcepath) $(sourcepath)/$(packages)/$(main).java
	touch $(name).manifest
	echo $(manifestline) >  $(name).manifest 
	jar -cfm $(name).jar $(name).manifest -C $(classpath) .
	rm $(name).manifest

.PHONY : clean

clean : 
	clear
	(cd bin ; rm -rf $(classpath)/*)
	rm $(name).jar