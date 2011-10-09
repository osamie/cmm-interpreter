

all:
	java -cp ssCC.jar ssCC --prefix CMM cmm.t cmm.g
	javac *.java

run:	all
	java CMM

