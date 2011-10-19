#!/bin/bash

java -cp SiCC.jar SiCC --prefix CMM cmm.t cmm.g
javac -d ../bin/ *.java

cd ../bin 

java CMM simple.cmm

#java CMMASTTdd

cd ../src


#java ../bin/CMM.class ../bin/simple.cmm 



