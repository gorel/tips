#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <resolv.h>
#include <arpa/inet.h>

#define MAXBUF          1024

int main(int argc, char **argv)
{
	if (argc != 4)
		exit(-1);
	
	int sockfd, n;
    struct sockaddr_in dest;
    char buffer[MAXBUF];
	
	int port = atoi(argv[1]);
	char *hostname = argv[2];
	char *request = argv[3];

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

    printf("\nConnected to %s on port %d\n\n", hostname, port);
	
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
