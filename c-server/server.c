#include <netdb.h> 
#include <syslog.h>
#include <string.h>
#include <netinet/in.h> 
#include <stdlib.h> 
#include <string.h> 
#include <sys/socket.h> 
#include <sys/types.h> 
#define MAX 80 
#define PORT 55752 
#define SA struct sockaddr 
  
// Function designed for chat between client and server. 
void func(int sockfd) 
{ 

    	syslog(LOG_INFO, "inside function method");
    	char buff[MAX]; 
    	int n; 
    	// infinite loop for chat 
    	for (;;) { 
        	bzero(buff, MAX); 
  
        	// read the message from client and copy it in buffer 
        	read(sockfd, buff, sizeof(buff)); 
        	// print buffer which contains the client contents 
		syslog(LOG_INFO, "message arrived from McGyver");
		syslog(LOG_INFO , buff); 
		syslog(LOG_INFO, "after meassage arrived...");	

		/**
        	bzero(buff, MAX); 
        	n = 0; 
        	// copy server message in the buffer 
        	while ((buff[n++] = getchar()) != '\n') 
            	; 
  
        	// and send that buffer to client 
        	write(sockfd, buff, sizeof(buff)); 
  
        	// if msg contains "Exit" then server exit and chat ended. 
        	if (strncmp("exit", buff, 4) == 0) { 
            		syslog("", "server exit"); 
            		break; 
        	} 
		*/
    	} 


} 
  
// Driver function 
int main(void) 
{ 
    openlog ("server", LOG_CONS | LOG_PID | LOG_NDELAY, LOG_LOCAL1);
    syslog(LOG_INFO, "Group One: Benjamin, Mikey and Louay");

    int sockfd, connfd, len; 
    struct sockaddr_in servaddr, cli; 
  
    // socket create and verification 
    sockfd = socket(AF_INET, SOCK_STREAM, 0); 
    if (sockfd == -1) { 
        syslog(LOG_INFO, "socket creation failed...\n");
        exit(0); 
    } 
    else
	syslog(LOG_INFO, "Socket successfully created..\n");


    bzero(&servaddr, sizeof(servaddr)); 
  
    // assign IP, PORT 
    servaddr.sin_family = AF_INET; 
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY); 
    servaddr.sin_port = htons(PORT); 
  
    // Binding newly created socket to given IP and verification 
    if ((bind(sockfd, (SA*)&servaddr, sizeof(servaddr))) != 0) { 
  	syslog(LOG_INFO, "Binding newly created socket t...");
	exit(0);
    } 
    else
	syslog(LOG_INFO, "Socket successfully binded..\n");
  
    // Now server is ready to listen and verification 
    if ((listen(sockfd, 5)) != 0) { 
        syslog(LOG_INFO, "listen failed...");
        exit(0); 
    } 
    else
        syslog(LOG_INFO, "server is listening...");
    len = sizeof(cli); 
  
    // Accept the data packet from client and verification 
    connfd = accept(sockfd, (SA*)&cli, &len); 
    if (connfd < 0) { 
        syslog(LOG_INFO, "server accepted failed...\n");
        exit(0); 
    } 
    else
        syslog(LOG_INFO, "server finally accepted the CLIENT...");
  
    // Function for chatting between client and server 
    func(connfd); 
  
    // After chatting close the socket
    close(sockfd); 

}

