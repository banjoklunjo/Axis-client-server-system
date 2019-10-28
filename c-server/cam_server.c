#include <syslog.h>
#include<stdio.h> 
#include<math.h> 
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
#include "cam_server.h"

char client_message[2000];

char cln_pKey_e[2000];
char cln_pKey_n[2000];

//double e; 
//double n;
//double phi; 

char stop_message[4];
char *stop_arr = "stop";
int is_stop_requested = 0;

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

		if( i >= 50)
		{
			isMore = 0;
		}
	}
	return 0;
}





void * socketThread(void *arg)
{
	
	int newSocket = *((int *)arg);
	char *msg;

 

	//Receive the e-part & n-part of client public key
	recv(newSocket , cln_pKey_e , 2000 , 0);
	recv(newSocket , cln_pKey_n , 2000 , 0);
	syslog(LOG_INFO, cln_pKey_e); 
	syslog(LOG_INFO, cln_pKey_n); 

	//Generate pserver public key as e and n
	double *keys;
	keys = generate_pub_key();		//First value n; Second value e; Third value Phi;

    	int k = 2;  // A constant value 
    	double d = (1 + (k*keys[2]))/keys[1]; 

	//Send the ee-part & nn-part of server public key



	char arr[50];
	syslog(LOG_INFO, "E");
	sprintf(arr,"%zu\n",keys[1]);
	syslog(LOG_INFO, arr);
	write(newSocket, arr, strlen(arr));
	write(newSocket, "\n", strlen("\n"));
	memset(arr, 0, strlen(arr)); 

	syslog(LOG_INFO, "N");
	sprintf(arr, "%d", keys[0]); 
	write(newSocket, arr, strlen(arr));
	write(newSocket, "\n", strlen("\n"));
	syslog(LOG_INFO, arr);
	memset(arr, 0, strlen(arr)); 


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
	void     *data;
	size_t   img_size;
	int row = 0;
	syslog(LOG_INFO, "after int row.....");    

	//Opens a stream to the camera to get the img
	stream = capture_open_stream(IMAGE_JPEG, client_message); 

	is_stop_requested = 0;
	while(is_stop_requested) { 
		//Receive message
		recv(newSocket , stop_message , 5 , 0);
		if(stop_arr == stop_message)
			is_stop_requested = 0;
		
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

		//Send the image data to the client
		int error = write(newSocket, row_data, sizeof(row_data));
		if(error == 0)
			break;
		//Checking if the write generate_pub_keyfailed
		//Might then be that the client is disconnected, so we break out of the loop
	
 	}
}

  

  


  

  

  



double *generate_pub_key()
{

	double n, e, phi;
	double p = (rand() % (50 - 0 + 1)) + 0;
	double q = (rand() % (50 - 0 + 1)) + 0;

	// First part of public key: 
	n = p*q; 

	// Finding other part of public key. 
	// e stands for encrypt 
	e = (rand() % (50 - 0 + 1)) + 0;
	phi = (p-1)*(q-1); 
	while (e < phi) 
	{ 
		// e must be co-prime to phi and 
		// smaller than phi. 
		if ( gcd(e, phi) == 1 ) 
		    break; 
		else
		    e++; 
	} 
	//4294856960
	// Two random prime numbers 
	double rettt[3];
	rettt[0] = n;
	rettt[1] = e;
	rettt[2] = phi;
	return rettt;
	//Now we have e and n
}

/*double generate_pub_key()
{
    // Two random prime numbers 
    double p = (rand() % (50 - 0 + 1)) + 0;
    double q = (rand() % (50 - 0 + 1)) + 0;


    // First part of public key: 
    n = p*q; 

    // Finding other part of public key. 
    // e stands for encrypt 
    e = (rand() % (50 - 0 + 1)) + 0;
    phi = (p-1)*(q-1); 
    while (e < phi) 
    { 
        // e must be co-prime to phi and 
        // smaller than phi. 
        if ( gcd(e, phi) == 1 ) 
            break; 
        else
            e++; 
    } 

    //Now we have e and n

}*/

// Returns gcd of a and b 
int gcd(int a, int h) 
{ 
    int temp = 0; 
    while (1) 
    { 
        temp = a%h; 
        if (temp == 0) 
          return h; 
        a = h; 
        h = temp; 
    } 
    return temp;
} 


/*double encryptXOR(double eValue, double nValue, double xorKey)
{
	double result = 0;
        // Encryption result = (xorKey ^ eValue) % nValue 
	result = pow(xorKey, eValue);
	result = fmod(result, nValue);
	return result;
}

double decryptXOR(double dValue, double nValue, double xorKey)
{
	double result = 0;
        // Encryption result = (xorKey ^ dValue) % nValue 
	result = pow(xorKey, dValue);
	result = fmod(result, nValue);
	return result;
}*/



// Message encrypting with XoR key
char *encrypt_char(char *message, char* key){
   int message_length = strlen(message);
   int key_length = strlen(key);
   char* encrypt_msg = malloc(message_length+1);
   int i;
   for ( i = 0; i< message_length; i++){
       encrypt_msg[i] = message[i] ^( key[i%key_length]-48); //Encrypts message to another array with the xor key generated (-48 for int to ascii conversion)
   }
   encrypt_msg[message_length]='\0';
   return encrypt_msg;
}
