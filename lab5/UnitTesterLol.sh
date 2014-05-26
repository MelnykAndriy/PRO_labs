#!/bin/bash

rm UnitTest.log

for i in {1..50}
do
    make -f makefile
done


java -jar PassedCounter.jar UnitTest.log