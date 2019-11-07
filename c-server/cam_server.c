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
#include "cam_server.h"
#include <capture.h>

char client_message[2000];

char buffer[1024];

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;



int start_up_server(void)
{
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
	int isMore = 1;
	while(isMore)
	{
		//Accept call creates a new socket for the incoming connection
		addr_size = sizeof serverStorage;
		newSocket = accept(serverSocket, (struct sockaddr *) &serverStorage, &addr_size);

		//for each client request creates a thread and assign the client request to it to process
		//so the main thread can entertain next request
		if( pthread_create(&tid[i], NULL, socketThread, &newSocket) != 0 )
			syslog(LOG_INFO, "Failed to create thread \n");
        
            	i++;

		if( i > 50)
            		isMore = 0;
	}
	return 0;
}





void * socketThread(void *arg)

{
	int newSocket = *((int *)arg);
	char *msg;


	recv(newSocket , client_message , 2000 , 0);

	/*''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''*/
	syslog(LOG_INFO, "RECIEVED n...\n");
	int n = atoi(client_message);
	syslog(LOG_INFO, "CONVERTED n...\n");
	recv(newSocket , client_message , 2000 , 0);
	syslog(LOG_INFO, "RECIEVED e...\n");
	int e = atoi(client_message);
	syslog(LOG_INFO, "CONVERTED e...\n");
	/*''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''*/
	int m = 123;

	long int rsa = ( fmod( pow( m, e) , n ) );

	char char_rsa[1000];

	//itoa(rsa, char_rsa, 1000);

	sprintf(char_rsa, "%ld", rsa);

	

	//snprintf(msg, sizeof( rsa ) , "%d", rsa);
	write(newSocket, char_rsa, strlen(char_rsa));   
	write(newSocket, "\n", strlen("\n"));
	syslog(LOG_INFO, "XOR SENT TO CLIENT ...\n"); 

	char *xor = "123";


	//Get all available resolutions on the camera
	msg = capture_get_resolutions_list(0);  

	//Send the resolutions to the client
	write(newSocket, msg, strlen(msg));   

	//Send a breakline to client, else the client wont read the message
	write(newSocket, "\n", strlen("\n"));   

	//Clear/empty the msg variable
	memset(msg, 0, strlen(msg));  

	media_stream *stream;
	syslog(LOG_INFO, "Thread CREATED ...  \n");

	recv(newSocket , client_message , 2000 , 0);
	syslog(LOG_INFO, "message below from client: ");
	syslog(LOG_INFO, client_message);

	syslog(LOG_INFO, client_message);

	media_frame  *frame;
	char     *data;
	size_t   img_size;
	int row = 0;
	syslog(LOG_INFO, "after int row.....");    
	int is_stop_requested = 1;

	//Opens a stream to the camera to get the img
	stream = capture_open_stream(IMAGE_JPEG, client_message); 
	int val = 0;
	char stop_message[5];
	char stop_arr[5] = "stop";

	while(1) {
		//Receive message
		//recv(newSocket , stop_message , 5 , 0);
		//if(stop_arr == stop_message)
		//	is_stop_requested = 0;
		//Get the frame
		frame = capture_get_frame(stream);    

		//Get image data
		data = capture_frame_data(frame);  

		//Get the image size
		img_size  = capture_frame_size(frame);    

		//Convert the image size to a char * to send to the client
		sprintf(msg,"%zu\n",img_size); 

		//Send the size to the client   
		write(newSocket, msg, strlen(msg));    

		sprintf(msg,"%zu\n", strlen(msg)); 
		syslog(LOG_INFO, "Storlek på storlek-stringen"); 
		syslog(LOG_INFO, msg); 
		sprintf(msg,"%zu\n",img_size);   

		syslog(LOG_INFO, msg);    
		unsigned char row_data[img_size]; 

		for(row = 0; row < img_size; row++){
			row_data[row] = ((unsigned char*)data)[row];
		}

                int size_image = strlen(data);
		data = encrypt_char(data, xor, img_size);

		//decrypting
		//data = encrypt_char(data, "ABC", img_size);

		//Send the image data to the client
		int error = write(newSocket, data, sizeof(row_data));

		//Checking if the write failed
		//Might then be that the client is disconnected, so we break out of the loop
		if (error < 0)
			syslog(LOG_INFO, "Client is disconnected");

		val++;


		//Emptying the variables to be sure nothing is stored 
		memset(data, 0, sizeof(data));
		memset(row_data, 0, sizeof(row_data));
		capture_frame_free(frame);

	}
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


char *encrypt_char(char *message, char *key, int img_size){
   // int message_length = strlen(message);
   int key_length = strlen(key);
   char* encrypt_msg = malloc(img_size+1);
   int i;
   for ( i = 0; i< img_size; i++){
       encrypt_msg[i] = message[i] ^( key[i%key_length]-48); 
   }
   encrypt_msg[img_size]='\0';
   return encrypt_msg;
}

	





