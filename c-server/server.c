#include <syslog.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h> // for open
#include <pthread.h>
#include <capture.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>
#include <sys/time.h>
#include <capture.h>


#define _GNU_SOURCE
char client_message[2000];

char buffer[1024];

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;



void * socketThread(void *arg)

{

    int newSocket = *((int *)arg);

    char *msg;
    msg = capture_get_resolutions_list(0);    //Get all available resolutions on the camera
    syslog("resolutions: ", msg);
    write(newSocket, msg, strlen(msg));        //Send the resolutions to the client
    write(newSocket, "\n", strlen("\n"));  //Send a breakline to client, else the client wont read the message
    memset(msg, 0, strlen(msg));        //Clear/empty the msg variable
    media_stream *stream;

    syslog(LOG_INFO, "Thread CREATED ...  \n");

 

    recv(newSocket , client_message , 2000 , 0);

    //syslog(LOG_INFO, client_message);
    //syslog(LOG_INFO, "REAL DEAL: resolution=160x120&sdk_format=Y800&fps=15\"");
    //Variables used for handling the img
    strcpy(client_message, "resolution=320x240&fps=15");


    media_frame  *frame;
    void     *data;
    size_t   img_size;
    int row = 0;
        

    stream = capture_open_stream(IMAGE_JPEG, client_message); //Opens a stream to the camera to get the img


    frame = capture_get_frame(stream);    //Get the frame
    data = capture_frame_data(frame);    //Get image data
    img_size  = capture_frame_size(frame);    //Get the image size
            
     sprintf(msg,"%zu\n",img_size);        //Convert the image size to a char * to send to the client
     write(newSocket, msg, strlen(msg));    //Send the size to the client
         
     //Now we loop the whole data array and write to another array (Not necessary, could send the data directly I think)                   
     unsigned char row_data[img_size];        
     for(row = 0; row<img_size;row++){
       row_data[row] = ((unsigned char*)data)[row];
     }

     //Send the image data to the client
     int error = write(newSocket, row_data, sizeof(row_data));

     //Checking if the write failed
     //Might then be that the client is disconnected, so we break out of the loop
     if (error < 0)
        syslog(LOG_INFO, "Client is disconnected");





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


