#include <ctype.h>
#include <unistd.h>
#include "base64.h"
#include <syslog.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h> // for open
#include <pthread.h>
#include <capture.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>
#include <sys/time.h>
#include <arpa/inet.h>
#include <math.h>

/*------------------------------------------------------------------------*/
// Function Prototypes Requried To Avoid Implicit Declaration Warning

void send_xor_key();

//void send_xor_encrypted_message();

char* xor_message(char* message, int message_length);

void generate_xor_key();

int init_socket();

void send_message_to_client( int socket, char* message );

void * socket_thread(void *arg);

char *encrypt_char(char *message, char *key, int img_size);
/*------------------------------------------------------------------------*/



/*---------------------------- VARIABLES ---------------------------------*/
#define PADDING RSA_PKCS1_PADDING
#define PORT_NUMBER 55777
#define XOR_KEY_LENGTH 8

int server_socket;
int client_socket;

char xor_key[XOR_KEY_LENGTH];
char str_public_key_client[300];
/*------------------------------------------------------------------------*/




/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int main( int argc, const char* argv[] )
{
  openlog("server", LOG_CONS | LOG_PID | LOG_NDELAY, LOG_LOCAL1);

  init_socket();

  close(server_socket);

  return 0;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
void * socket_thread(void *arg) 
{
	syslog(LOG_INFO, "socket_thread");
	int socket_client_thread = *(int*)arg;
	char client_message[2000];
        char new_line = '\n';
        
	// receive public key from client
        recv(socket_client_thread , client_message , 2000 , 0);
        long long int n = atoi(client_message); // n
        memset(client_message, 0, 2000);
	syslog(LOG_INFO, "socket_thread: received n --> %lld", n);
        recv( socket_client_thread , client_message , 2000 , 0 );
        int e = atoi( client_message ); // e
        memset(client_message, 0, 2000);
	syslog(LOG_INFO, "socket_thread: received e --> %d", e);

	// send RSA encrypted xor key
	int XoR_Key = 5;
        double pow_value = pow( XoR_Key,e );
        double fmod_value = fmod( pow_value, n );       
	sprintf(client_message, "%d", (int) fmod_value);
	syslog(LOG_INFO, "socket_thread: sending RSA encrypted Xor key --> %s", client_message);
      
	strncat(client_message, &new_line, 1); 
        write( socket_client_thread, client_message, strlen(client_message) ) ;

        // send resolutions
        char *resolutions = capture_get_resolutions_list(0);
        strncat(resolutions, &new_line, 1); 
	syslog(LOG_INFO, "socket_thread: sending resolutions --> %s", resolutions);
        write( socket_client_thread, resolutions, strlen(resolutions) ) ;

        // start sending image
	media_stream *stream;
        media_frame  *frame;
        char *data;

        size_t img_size;
	
	memset(client_message, 0, 2000);
	recv(socket_client_thread , client_message , 2000 , 0);
	syslog(LOG_INFO, "socket_thread: receive resolution and fps --> %s", client_message);

	stream = capture_open_stream(IMAGE_JPEG, client_message);
	syslog(LOG_INFO, "socket_thread: capture open stream"); 

	while(1) 
        {
                syslog(LOG_INFO, "while-loop: capture get frame");
		frame = capture_get_frame(stream);    

		//Get image data
		data = capture_frame_data(frame);
         	syslog(LOG_INFO, "while-loop: Get image data");  

		//Get the image size
		img_size  = capture_frame_size(frame); 
		syslog(LOG_INFO, "while-loop: Get the image size --> %zu", img_size);     

		//Convert the image size to a char * to send to the client
                char message[10] = "";
		sprintf( message,"%zu" ,img_size );
		syslog(LOG_INFO, "while-loop: Convert the image size to a char array --> %s", message);   

		//Send the size to the client
		syslog(LOG_INFO, "while-loop: sending size to client");
        	strncat(message, &new_line, 1); 
        	write( socket_client_thread, message, strlen(message) ) ;

		//data = encrypt_char(data, XoR_Key, sizeof(data));

                int bytes_sent = write(socket_client_thread, data, img_size);
		syslog(LOG_INFO, "while-loop: sending image to client [%d bytes sent]", bytes_sent); 
                
                /*while (length > 0)
    		{
        		int i = write(socket_client_thread, ptr, length);
        		if (i < 1) return false;
        		ptr += i;
        		length -= i;
    		}*/ 

		//Emptying the variables to be sure nothing is stored 
		memset(data, 0, sizeof(data));
		capture_frame_free(frame);

	}
}


/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
/* If a message character happens to match the xor key character, the result will be 0.
 * A 0 (null-terminator char) denotes the end of the string. 
 * If this happens, the function send_message_to_client wont work properly because
 * it takes strlen of the message to be send and strlen looks for the null-terminator,
 * which in this case can be in the middle of the message. As a result of this only a
 * part of the xor:ed string is sent.
 *
 * EVERY CHARACTER EXPECT UPPERCASE ALPHABET CHARACTER IS ALLOWED TO BE SENT TO THE CLIENT
 * 
 * Better explanation: 
 *   - https://stackoverflow.com/questions/44720995/xor-encrypted-message-length-is-showing-wrongly-in-c 
 */
char* xor_message(char* message, int message_length)
{
    int xor_key_length = strlen( xor_key );

    char* encrypted = malloc( message_length + 1 );
    
    int i;
    for(i = 0; i < message_length; i++)
    {
        encrypted[i] = message[i] ^ xor_key[ i % xor_key_length ];
    }
    
    encrypted[message_length] = '\0';

    return encrypted;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
void generate_xor_key()
{
  srand( time( NULL ) );
  
  char abc[26] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  int i; 
  for (i = 0; i < XOR_KEY_LENGTH; ++i)
  {
    xor_key[i] = abc[rand() % (sizeof(abc) - 1)];
  }
  
  printf("\nxor key: %s\n", xor_key);
  
  xor_key[XOR_KEY_LENGTH] = '\0';
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int init_socket() 
{
  server_socket = socket(AF_INET, SOCK_STREAM, 0);

  struct sockaddr_in server_address;
  server_address.sin_family = AF_INET;
  server_address.sin_port = htons(PORT_NUMBER);
  server_address.sin_addr.s_addr = htonl(INADDR_ANY);

  bind(server_socket, (struct sockaddr *) &server_address, sizeof(server_address));

  if(listen(server_socket,50)==0) syslog(LOG_INFO, "Server Is Listening...\n");
  else syslog(LOG_INFO, "Error: Server Is Not Listening\n");

  pthread_t tid[60];
  int i = 0;
  int thread_limit = 1;

  while(thread_limit)
  {
	client_socket = accept(server_socket, NULL, NULL);
    
	if(client_socket > -1) 
 	{
		syslog(LOG_INFO,"A client has connected to the server\n");

        	if( pthread_create(&tid[i], NULL, socket_thread, &client_socket) != 0 )
		{
			syslog(LOG_INFO, "Failed to create thread \n");
		}
		
		i++;

		if( i > 50 )
            		thread_limit = 0;
        
  	}
  	else syslog(LOG_INFO,"Error: A client was not able to connect to the server\n");
  }
  
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
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
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
/*
 * The function appends a new-line ('\n') character to the message because the client 
 * is using BufferedReader.readLine method which reads a line of text until the
 * terminator char which is '\n'.
 */
void send_message_to_client(int socket, char* message)
{
  char new_line = '\n';
  strncat(message, &new_line, 1); 

  int bytes_sent = write( socket, message, strlen(message) ) ;
  
  if(bytes_sent > 0)
  {
    //printf("--> [%d bytes was sent]: %s\n", bytes_sent, message);
  }
  
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
