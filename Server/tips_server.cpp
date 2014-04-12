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
#include <unistd.h>

#define DEFAULT_PORT		9313
#define THREAD_POOL_COUNT	5
#define BUF_SIZE		1024
#define MAXQUEUE		50

#define HTML_TYPE	0
#define GIF_TYPE	1
#define TEXT_TYPE	2

#define GET	1
#define POST	2

#define FPROC	1
#define FTHRD	2
#define FPOOL	4
int FLAGS = 0;

typedef struct
{
	int fd;
	pthread_mutex_t mutex;
	int master_socket;
	int tnum;
} ARGS;

const char *error404 =		"HTTP/1.1 404 File Not Found\r\n"
				"Server: CS252 lab5\r\n"
				"Content-type: text/html\r\n"
				"\r\n"
				"<html><title>404 Page Not Found</title><body><h1>The requested page could not be found.</h1></body>\r\n";

const char *error403 = 		"HTTP/1.1 403 Forbidden\r\n"
				"Server: CS252 lab5\r\n"
				"Content-type: text/html\r\n"
				"\r\n"
				"<html><title>403 Forbidden</title><body><h1>Read access forbidden.</h1></body>\r\n";

const char *errorsecurity =	"HTTP/1.1 404 File Not Found\r\n"
				"Server: CS252 lab5\r\n"
				"Content-type text/html\r\n"
				"\r\n"
				"<html><title>Security warning</title><body><h1>Error: the requested file is above the root directory.</h1></body>\r\n";

const char *response_text =	"HTTP/1.1 200 Document follows\r\n"
				"Server: CS252 lab5\r\n";

const char *htmlcontent =	"Content-type: text/html\r\n";
const char *gifcontent = 	"Content-type: image/gif\r\n";
const char *textcontent = 	"Content-type: text/plain\r\n";

const char *htmlheader =	"<html><body>";
const char *htmlfooter =	"</body></html>";

const char *BASEDIR = 		"/http-root-dir/htdocs";
const char *ICONBASEDIR = 	"/http-root-dir/icons";
const char *CGIBASEDIR = 	"/http-root-dir/cgi-bin/";


//Accept routine for pooled threads
void *accept_POOL(void *argstruct);

//Send the newline signal to an HTTP request
void write_newline(int fd);

//call perror and exit
void exit_with_error(char *message);

//Helper function to process a request from a new thread
void *process_request_THREAD(void *argstruct);

//Process a document request
void process_request(int fd);

//Add the POST values to the filestr
void add_POST_extension(char *filestr, char *readbuf);

//Check if the given path is above the virtual root
char file_above_virtual_root(char *file);

//Send a 403 Forbidden warning to the user for trying to access above the virtual root
void send_security_response(int fd);

//Send a response to the user on the given fd
void send_response(int fd, char *file);

//Send a response of the directory listing
void write_directory_response(int fd, char **entries, int num_entries, char *sourcedir);

//Send a response of the file requested
void write_file_response(int fd, FILE *f, int filetype);

//Send a response of the executed cgi-bin file
void execute_cgi_bin(int fd, char *file, char req_type);

//Sort strings for qsort
int strsort(const void *a, const void *b);

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
	{
		if (!strcmp(argv[i], "-f"))
		{
			FLAGS |= FPROC;
			printf("FORK PROCESS FLAG SPECIFIED.\n");
		}
		else if (!strcmp(argv[i], "-t"))
		{
			FLAGS |= FTHRD;
			printf("CREATE THREAD FLAG SPECIIFED.\n");
		}
		else if (!strcmp(argv[i], "-p"))
		{
			FLAGS |= FPOOL;
			printf("POOLED THREAD FLAG SPECIFIED.\n");
		}
		else
		{
			port = atoi(argv[i]);
		}
	}
	
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
	if (FLAGS & FPOOL)
	{
		pthread_t TPOOL[THREAD_POOL_COUNT];
		ARGS args[THREAD_POOL_COUNT];
		
		//Create and start each thread
		for (int i = 0; i < THREAD_POOL_COUNT; i++)
		{
			args[i].master_socket = master_socket;
			args[i].tnum = i+1;

			pthread_create(&TPOOL[i], NULL, accept_POOL, &args[i]);
		}

		//Wait for each thread to finish (Should be never...)
		for (int i = 0; i < THREAD_POOL_COUNT; i++)
			pthread_join(TPOOL[i], NULL);

		//return from main
		close(master_socket);
		return 0;
	}

	//Otherwise, proceed by having the main thread accept connections
	//Accept connections and process them one by one
	while ("The Earth is flat.")
	{
		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof(clientIPAddress);
		int slave_socket = accept(master_socket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);
		if (slave_socket < 0) exit_with_error((char *)"accept");

		//If the fork process flag was specified, fork a child and have it process the request
		if (FLAGS & FPROC)
		{
			if (fork() == 0)
			{
				process_request(slave_socket);
				close(slave_socket);
				exit(0);
			}
		}
		//If the thread flag was specified, create a new thread to process the request
		else if (FLAGS & FTHRD)
		{
			pthread_t thread;
			ARGS args;
			args.fd = dup(slave_socket);
			pthread_create(&thread, NULL, process_request_THREAD, &args);
		}
		//Otherwise, the main thread will handle the request
		else
			process_request(slave_socket);

		// Close socket
		close(slave_socket);
	}

	//This code will never be reached since the server should run infinitely
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

void *
process_request_THREAD(void *argstruct)
{
	ARGS *args = (ARGS *)argstruct;
	process_request(args->fd);
	close(args->fd);
	return 0;
}

void
process_request(int fd)
{
	char fullpath[BUF_SIZE];
	char filestr[BUF_SIZE];
	char *file;

	//Read the client's request
	char readbuf[BUF_SIZE] = "";
	read(fd, readbuf, BUF_SIZE);
	
	//Get the content directory
	getcwd(fullpath, BUF_SIZE);

	//Parse the filename from the HTTP GET/POST request
	char req_type;
	if (strstr(readbuf, "GET"))
	{
		sscanf(readbuf, "GET %s HTTP/1.1\n", filestr);
		req_type = GET;
	}
	else
	{
		sscanf(readbuf, "POST %s HTTP/1.1\n", filestr);
		add_POST_extension(filestr, readbuf);
		req_type = POST;
	}

	//See if the user specified the icons directory
	if (!strncmp(filestr, "/icons", strlen("/icons")))
	{
		strcat(fullpath, ICONBASEDIR);
		file = filestr + strlen("/icons");
	}
	else
	{
		strcat(fullpath, BASEDIR);
		file = filestr;
	}

	//If cgi-bin wasn't specified, set the path to the htdocs directory
	if (!strncmp(file, "/cgi-bin/", strlen("/cgi-bin/")))
		execute_cgi_bin(fd, file, req_type);
	//If the user is trying to access above the virtual root, send them a 403 Forbidden message
	else if (file_above_virtual_root(file))
		send_security_response(fd);
	//The user is requesting a general file
	else
	{
		//If the user requested the root index, reply with index.html
		if (!strcmp(file, "/"))
			strcat(file, "index.html");
		//Else if the user requested to browse the directory tree, send them the root directory
		else if (!strcmp(file, "/__BROWSE__"))
			strcpy(file, "/");
		
		//Concatenate the fullpath with the requested file
		strcat(fullpath, file);

		//Send the requested document to the user
		send_response(fd, fullpath);
	}
}

void
add_POST_extension(char *filestr, char *readbuf)
{
	printf("POST!\n");
}

char
file_above_virtual_root(char *file)
{
	int level = 0;
	while (*file)
	{
		//User is going into a new directory
		if (*file == '/')
		{
			if (*(file + 1) == '.' && *(file + 2) == '.')
				level--;
			else
				level++;

			//User is now above virtual root
			if (level < 0)
				return 1;
		}
		file++;
	}

	//User never went above virtual root
	return 0;
}

void
send_security_response(int fd)
{
	write(fd, error403, strlen(error403));
}

void
send_response(int fd, char *file)
{
	struct stat s;
	if (stat(file, &s) == 0)
	{
		//User requested a directory
		if (s.st_mode & S_IFDIR)
		{
			//directory
			DIR *dir;
			struct dirent *ent;

			//Open the directory to read its contents
			if ((dir = opendir(file)) != NULL)
			{
				char *entries[512];
				int num_entries = 0;

				//Read each entry and add it to the list of entries
				while ((ent = readdir(dir)) != NULL)
					if (ent->d_name[0] != '.' || !strcmp(ent->d_name, ".."))
						entries[num_entries++] = strdup(ent->d_name);
				closedir(dir);

				//Sort the entries and send them to the client
				qsort(entries, num_entries, sizeof(entries[0]), strsort);
				if (strstr(file, BASEDIR))
					write_directory_response(fd, entries, num_entries, strstr(file, BASEDIR) + strlen(BASEDIR) + 1);
				else
				{
					char directory[512];
					sprintf(directory, "icons/%s", strstr(file, ICONBASEDIR) + strlen(ICONBASEDIR) + 1);
					write_directory_response(fd, entries, num_entries, directory);
				}

				//Free the malloced strings
				for (int i = 0; i < num_entries; i++)
					free(entries[i]);
			}
			else
			{
				write(fd, error404, strlen(error404));
			}
		}
		//User requested a file
		else if (s.st_mode & S_IFREG)
		{
			//file
			int filetype;
			if (strlen(file) > 5 && !strcmp(file + strlen(file) - 5, ".html"))
				filetype = HTML_TYPE;
			else if (strlen(file) > 4 && !strcmp(file + strlen(file) - 5, ".gif"))
				filetype = GIF_TYPE;
			else
				filetype = TEXT_TYPE;

			FILE *f = fopen(file, "r");
			write_file_response(fd, f, filetype);
		}
		//No clue wtf this is
		else
		{
			//No clue wtf this is
			write(fd, error404, strlen(error404));
		}
	}
	//File does not exist
	else
	{
		write(fd, error404, strlen(error404));
	}
}

void
write_directory_response(int fd, char **entries, int num_entries, char *sourcedir)
{
	//Write a response that html content will follow to a response string
	char response[2048];
	sprintf(response, "%s%s\r\n", response_text, htmlcontent);
	
	//Write the response and html header
	write(fd, response, strlen(response));
	write(fd, htmlheader, strlen(htmlheader));

	//Write a header stating the directory being opened
	sprintf(response, "<h1>Opened directory: <b>%s</b></h1>", strlen(sourcedir) == 0 ? "/" : sourcedir);
	write(fd, response, strlen(response));

	//Write the bulletted list header
	write(fd, "\t<ul>\n", strlen("\t<ul>\n"));

	//For each entry in the directory...
	char entry[512];
	for (int i = 0; i < num_entries; i++)
	{
		//Make sure the source directory doesn't end in a '/'
		char *lastchar = sourcedir + strlen(sourcedir) - 1;
		if (*lastchar == '/') *lastchar = '\0';

		//If we're in the root directory, just output "/<file>"
		if (strlen(sourcedir) == 0)
			sprintf(entry, "\t<li><a href=\"%s\">%s</a><br>\n", entries[i], entries[i]);
		//Else output "<sourcedir>/<file>"
		else
			sprintf(entry, "\t<li><a href=\"/%s/%s\">%s</a><br>\n", sourcedir, entries[i], entries[i]);

		//Write the bullet entry to the user	
		write(fd, entry, strlen(entry));
	}

	//End the list and write the html footer
	write(fd, "\t</ul>", strlen("\t</ul>"));
	write(fd, htmlfooter, strlen(htmlfooter));
}

void
write_file_response(int fd, FILE *f, int filetype)
{
	char readbuf[BUF_SIZE];
	int n;

	//Write the response header
	write(fd, response_text, strlen(response_text));

	//Write the content type depending on the requested file
	if (filetype == HTML_TYPE)
		write(fd, htmlcontent, strlen(htmlcontent));
	else if (filetype == GIF_TYPE)
		write(fd, gifcontent, strlen(gifcontent));
	else
		write(fd, textcontent, strlen(textcontent));
	
	//Write the carriage return to signal that the document will now follow
	write_newline(fd);

	//Write the file
	while ((n = fread(readbuf, sizeof(readbuf[0]), BUF_SIZE, f)))
		write(fd, readbuf, n);
}

void
write_newline(int fd)
{
	write(fd, "\r\n", strlen("\r\n"));
}

void
execute_cgi_bin(int fd, char *file, char req_type)
{
	//Fork a child process
	if (fork() == 0)
	{
		//Redirect output to the slave socket
		dup2(fd, STDOUT_FILENO);
		
		//Set the request method environment variable
		//TODO: Handle POST
		if (req_type == POST)
			setenv("REQUEST_METHOD", "POST", 1);
		else
			setenv("REQUEST_METHOD", "GET", 1);
	
		//Set the query string environment variable
		char *pos = strchr(file, '?');
		if (pos)
		{
			setenv("QUERY_STRING", pos + 1, 1);
			*pos = '\0';
		}
		else
			unsetenv("QUERY_STRING");

		//Write the response header
		write(fd, response_text, strlen(response_text));
	
		//Get the base directory of the cgi-bin scripts
		char directory[BUF_SIZE];
		getcwd(directory, BUF_SIZE);
		strcat(directory, CGIBASEDIR);
		strcat(directory, file + strlen("/cgi-bin/"));

		//Get the cgi-bin script to execute
		char *args[3];
		args[0] = directory;
		args[1] = pos ? pos + 1 : NULL;
		args[2] = NULL;


		//Execute the cgi-bin script
		execvp(args[0], args);
		send_security_response(fd);
	}
}

int
strsort(const void *a, const void *b)
{
	return strcmp(*(char **)a, *(char **)b);
}

void
killzombie(int a)
{
	pid_t pid;
	int status;
	while ((pid = waitpid(-1, &status, WNOHANG)) > 0);
}
