#include <dirent.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <pthread.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <unistd.h>

#define DEFAULT_PORT		9313
#define THREAD_POOL_COUNT	5
#define BUF_SIZE		1024
#define MAXQUEUE		50

#define ROOTDIR			"/comments/"

//Global write lock
pthread_mutex_t write_lock;

typedef struct
{
	int fd;
	pthread_mutex_t mutex;
	int master_socket;
	int tnum;
} ARGS;

//Accept routine for pooled threads
void *accept_POOL(void *argstruct);

//call perror and exit
void exit_with_error(char *message);

//Process a document request
void process_request(int fd);

//Send a response to the user on the given fd
void send_response(int fd, char *file);

//Send a response of the requested comment thread
void write_comment_response(int fd, FILE *f);

//Write the given comment to file and respond to the user whether or not it worked
void write_comment_to_file(int fd, FILE *f, char *name, char *comment);

//Action to kill zombie processes
void killzombie(int a);

int
main(int argc, char **argv)
{
	//Set up the signal handler for zombie processes
	struct sigaction sigchild;
	sigchild.sa_handler = killzombie;
	sigemptyset(&sigchild.sa_mask);
	sigchild.sa_flags = SA_RESTART;
	if (sigaction(SIGCHLD, &sigchild, NULL))
	{
		perror("sigaction");
		exit(1);
	}

	//Get the arguments to the program
	int port;
	for (int i = 1; i < argc; i++)
		port = atoi(argv[i]);
	
	//Get the hostname of this machine
	char hostname[1024];
	gethostname(hostname, 1024);

	//Get the port the server should run on
	if (port == 0) port = DEFAULT_PORT;
	
	//Set the IP address and port for this server
	struct sockaddr_in serverIPAddress; 
	memset(&serverIPAddress, 0, sizeof(serverIPAddress));
	serverIPAddress.sin_family = AF_INET;
	serverIPAddress.sin_addr.s_addr = INADDR_ANY;
	serverIPAddress.sin_port = htons((u_short) port);
	
	//Allocate a socket
	int master_socket = socket(PF_INET, SOCK_STREAM, 0);
	if (master_socket < 0) exit_with_error((char *)"socket");

	//Set socket options to reuse port. Otherwise we will
	//have to wait about 2 minutes before reusing the same port number
	int optval = 1; 
	int err = setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&optval, sizeof(int));
	if (err) exit_with_error((char *)"setsockopt");
	 
	//Bind the socket to the IP address and port
	err = bind(master_socket, (struct sockaddr *)&serverIPAddress, sizeof(serverIPAddress));
	if (err) exit_with_error((char *)"bind");
	
	//Put socket in listening mode and set the 
	//size of the queue of unprocessed connections
	err = listen(master_socket, MAXQUEUE);
	if (err) exit_with_error((char *)"listen");
	printf("Socket now listening at %s on port %d.\n", hostname, port);

	//If the pool flag was specified, create pooled threads to deal with incoming connections
	pthread_t TPOOL[THREAD_POOL_COUNT];
	ARGS args[THREAD_POOL_COUNT];

	//Initialize the write lock mutex 
	pthread_mutex_init(&write_lock, NULL);
	
	//Create and start each thread
	for (int i = 0; i < THREAD_POOL_COUNT; i++)
	{
		args[i].master_socket = master_socket;
		args[i].tnum = i+1;
		pthread_mutex_init(&args[i].mutex, NULL);
		pthread_create(&TPOOL[i], NULL, accept_POOL, &args[i]);
	}

	//Wait for each thread to finish (Should be never...)
	for (int i = 0; i < THREAD_POOL_COUNT; i++)
	{
		pthread_join(TPOOL[i], NULL);
		pthread_mutex_destroy(&args[i].mutex);
	}

	//Destroy the write lock mutex
	pthread_mutex_destroy(&write_lock);

	//return from main
	close(master_socket);
	return 0;
}

void
exit_with_error(char *message)
{
	perror(message);
	exit(-1);
}

void *
accept_POOL(void *argstruct)
{
	ARGS *args = (ARGS *)argstruct;
	pthread_mutex_t *mutex = &args->mutex;
	int master_socket = args->master_socket;
	int tnum = args->tnum;

	printf("Thread #%d starting.\n", tnum);

	while ("Yo mama is fat.")
	{
		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof(clientIPAddress);
	
		//Accept a new connection
		pthread_mutex_lock(mutex);
		int slave_socket = accept(master_socket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);
		pthread_mutex_unlock(mutex);
	
		//If the connection is invalid, exit
		if (slave_socket < 0) exit_with_error((char *)"accept");
	
		//Process the request
		process_request(slave_socket);
		close(slave_socket);
	}

	close(master_socket);
	return 0;
}

void
process_request(int fd)
{
	char fullpath[BUF_SIZE];
	char *filename;
	char *name;
	char *comment;
	FILE *file;
	char type;
	int i = 0;

	//Read which file was requested
	char readbuf[BUF_SIZE] = "";
	read(fd, readbuf, BUF_SIZE);

	//Scan the request to parse relevant information
	type = readbuf[0];
	while (readbuf[i++] != ' ');
	filename = readbuf + i;
	i++;
	while (readbuf[i] != ' ' && readbuf[i] != '\r' && readbuf[i] != '\n') i++;
	readbuf[i] = '\0';
	
	//Get the content directory
	getcwd(fullpath, BUF_SIZE);

	//Concatenate the path with the comment root directory
	strcat(fullpath, ROOTDIR);

	if (type == 'G') //User is requesting a comment thread
	{
		//Concatenate the fullpath with the requested file
		strcat(fullpath, filename);

		//Open the file for reading
		file = fopen(fullpath, "r");

		//Send the requested document to the user and close the file
		write_comment_response(fd, file);
	}
	else //User is posting a comment
	{
		//Retrieve the name of the user that is posting
		while (readbuf[i++] != ' ');
		name = readbuf + i;

		//Null out the previous character so the filename is accurate
		readbuf[i - 1] = '\0';

		//Retrieve the comment that the user is posting
		while (readbuf[i++] != ' ');
		comment = readbuf + i;

		//Null out the previous character so the name is accurate
		readbuf[i - 1] = '\0';

		//Concatenate the fullpath with the requested file
		strcat(fullpath, filename);

		//Lock the write lock so threads won't overwrite each others' data
		pthread_mutex_lock(&write_lock);

		//Open the file for appending
		file = fopen(fullpath, "a");

		//Append the user's comment to the file
		write_comment_to_file(fd, file, name, comment);

		//Unlock the write lock so other threads can write to file
		pthread_mutex_unlock(&write_lock);
	}

	//Close the file if it was opened
	if (file) fclose(file);
}

void
write_comment_response(int fd, FILE *f)
{
	char readbuf[BUF_SIZE];
	int n;

	//If the file was not opened, print out "NOTFOUND"
	if (!f)
	{
		write(fd, "NOTFOUND", strlen("NOTFOUND"));
		return;
	}

	//Write the file with buffered output
	while ((n = fread(readbuf, sizeof(readbuf[0]), BUF_SIZE, f)))
		write(fd, readbuf, n);
}

void
write_comment_to_file(int fd, FILE *f, char *name, char *comment)
{
	//If the file could not be opened, write "ERROR"
	if (!f)
	{
		write(fd, "ERROR", strlen("ERROR"));
		return;
	}

	//Get the current time
	char timestr[BUF_SIZE];
	time_t now = time(NULL);
	struct tm *t = localtime(&now);
	strftime(timestr, BUF_SIZE - 1, "%Y-%m-%d %H:%M", t);

	//Make sure there isn't a trailing newline in the comment
	if (comment[strlen(comment) - 1] == '\n')
		comment[strlen(comment) - 1] = '\0';

	//Write the user's comment to the file
	fprintf(f, "%s|%s|%s\n", name, timestr, comment); 

	//Tell the user their comment was posted by replying "SUCCESS"
	write(fd, "SUCCESS", strlen("SUCCESS"));
}

void
killzombie(int a)
{
	pid_t pid;
	int status;
	while ((pid = waitpid(-1, &status, WNOHANG)) > 0);
}
