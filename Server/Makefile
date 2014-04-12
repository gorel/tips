CXX= g++ -fPIC
NETLIBS= -lnsl
CFLAGS= -Wall -Werror -pedantic -g

all: myhttpd

myhttpd: myhttpd.o
	$(CXX) -o $@ $@.o $(NETLIBS) $(CFLAGS) -lpthread

%.o: %.cpp
	@echo 'Building $@ from $<'
	$(CXX) $(CFLAGS) -o $@ -c -I. $<

%.o: %.cc
	@echo 'Building $@ from $<'
	$(CXX) -o $@ -c -I. $<

clean:
	rm -f *.o use-dlopen hello.so
	rm -f *.o daytime-server

