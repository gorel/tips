#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <resolv.h>
#include <arpa/inet.h>

#define MAXBUF		  1024

int read_comment(char *buf, int n);

int main(int argc, char **argv)
{
	if (argc != 3)
	{
		printf("Usage: ./test_client <hostname> <port>\n");
		exit(-1);
	}

	int sockfd, n;
	struct sockaddr_in dest;
	char buffer[MAXBUF];
	
	char *hostname = argv[1];
	int port = atoi(argv[2]);
	char request[MAXBUF * 2];

	char type;
	char thread_num[MAXBUF];
	char name[MAXBUF];
	char message[MAXBUF];

	/*---Open socket for streaming---*/
	if ( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0 )
	{
		perror("Socket");
		exit(errno);
	}

	/*---Initialize server address/port struct---*/
	bzero(&dest, sizeof(dest));
	dest.sin_family = AF_INET;
	dest.sin_port = htons(port);

	/*---Connect to server---*/
	if ( connect(sockfd, (struct sockaddr*)&dest, sizeof(dest)) != 0 )
	{
		perror("Connect ");
		exit(errno);
	}

	/*---Parse user's request---*/
	printf("\nConnected to %s on port %d\n\n", hostname, port);
	printf("Input type ('G'et/'P'ost): ");
	fflush(stdout);
	scanf("%c", &type);
	printf("Input thread number: ");
	fflush(stdout);
	scanf("%s", thread_num);
	if (type != 'G')
	{
		/*---Get additional input from the user about their comment---*/
		printf("Input your name followed by EOF (Ctrl+D): ");
		fflush(stdout);
		scanf("%s\n", name);
		printf("Input your comment followed by EOF (Ctrl+D): ");
		fflush(stdout);
		read_comment(message, MAXBUF);

		/*---Create a message from the user's input---*/
		sprintf(request, "%c %s %s %s", type, thread_num, name, message);
	}
	else
	{
		/*---Create a message from the user's input---*/
		sprintf(request, "%c %s", type, thread_num);
	}

	/*---Send request---*/
	write(sockfd, request, strlen(request));

	/*---Get response---*/
	while ((n = read(sockfd, buffer, MAXBUF - 1)))
	{
		buffer[n] = '\0';
		printf("%s", buffer);
	}
	printf("\n");

	/*---Clean up---*/
	close(sockfd);
	return 0;
}

int
read_comment(char *buf, int n)
{
	int c, len = 0;
	while ((c = getchar()) != EOF && len < n)
		buf[len++] = c;
	return len;
}
