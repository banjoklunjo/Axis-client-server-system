
#include <syslog.h>
#include <string.h>
#include<stdio.h>

#include<stdlib.h>

#include<sys/socket.h>

#include<netinet/in.h>

#include<string.h>

#include <arpa/inet.h>

#include <fcntl.h> // for open

#include <unistd.h> // for close

#include<pthread.h>


char client_message[2000];

char buffer[1024];

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;



void * socketThread(void *arg)

{
	syslog(LOG_INFO, "Thread CREATED ...  \n");

  int newSocket = *((int *)arg);

  recv(newSocket , client_message , 2000 , 0);


  // Send message to the client socket 

  pthread_mutex_lock(&lock);

  char *message = malloc(sizeof(client_message)+20);

  strcpy(message,"Hello Client : ");

  strcat(message,client_message);

  strcat(message,"\n");

  strcpy(buffer,message);

  free(message);

  pthread_mutex_unlock(&lock);

  sleep(1);

  send(newSocket,buffer,50,0);


  syslog(LOG_INFO, buffer);

  syslog(LOG_INFO, "Exit socketThread\n");

  close(newSocket);

  pthread_exit(NULL);

}

int main(){

  openlog ("server", LOG_CONS | LOG_PID | LOG_NDELAY, LOG_LOCAL1);



  syslog(LOG_INFO, "Server running...");

  int serverSocket, newSocket;

  struct sockaddr_in serverAddr;

  struct sockaddr_storage serverStorage;

  socklen_t addr_size;


  //Create the socket. 

  serverSocket = socket(PF_INET, SOCK_STREAM, 0);



  // Configure settings of the server address struct

  // Address family = Internet 

  serverAddr.sin_family = AF_INET;



  //Set port number, using htons function to use proper byte order 

  serverAddr.sin_port = htons(55752);



  //Set IP address to localhost 

  serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);



  //Set all bits of the padding field to 0 

  memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);



  //Bind the address struct to the socket 

  bind(serverSocket, (struct sockaddr *) &serverAddr, sizeof(serverAddr));



  //Listen on the socket, with 40 max connection requests queued 

  if(listen(serverSocket,50)==0)
    syslog(LOG_INFO, "Listening\n");

  else
    syslog(LOG_INFO, "not Listening\n");



    pthread_t tid[60];

    int i = 0;

    while(1)

    {

        //Accept call creates a new socket for the incoming connection

        addr_size = sizeof serverStorage;

        newSocket = accept(serverSocket, (struct sockaddr *) &serverStorage, &addr_size);

        //for each client request creates a thread and assign the client request to it to process

       //so the main thread can entertain next request

        if( pthread_create(&tid[i], NULL, socketThread, &newSocket) != 0 )

           syslog(LOG_INFO, "Failed to create thread \n");

        if( i >= 50)

        {

          i = 0;

          while(i < 50)

          { 

            pthread_join(tid[i++],NULL);

          }

          i = 0;

        }

    }

  return 0;

}