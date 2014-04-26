CXX= g++ -fPIC
NETLIBS= -lnsl
CFLAGS= -Wall -Werror -pedantic -g

all: tips_server test_client SQLServer

SQLServer: SQLServer.java
	javac SQLServer.java

tips_server: tips_server.o
	$(CXX) -o $@ $@.o $(NETLIBS) $(CFLAGS) -lpthread

test_client: test_client.o
	$(CXX) -o $@ $@.o $(NETLIBS) $(CFLAGS)

%.o: %.cpp
	@echo 'Building $@ from $<'
	$(CXX) $(CFLAGS) -o $@ -c -I. $<

%.o: %.cc
	@echo 'Building $@ from $<'
	$(CXX) -o $@ -c -I. $<

clean:
	rm -f *.o tips_server test_client

