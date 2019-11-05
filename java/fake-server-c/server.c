#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <openssl/rsa.h>
#include <openssl/engine.h>
#include <openssl/pem.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include "base64.h" //

/*------------------------------------------------------------------------*/
// Function Prototypes Requried To Avoid Implicit Declaration Warning
int receive_client_public_key();

void send_xor_key();

void send_xor_encrypted_message();

char* xor_message(char* message, int message_length);

RSA* load_public_key_from_string( const char* public_key_string );

unsigned char* rsa_encrypt( RSA* public_key, const unsigned char* msg, int msg_length, int* encrypted_msg_length );

char* rsa_encrypt_then_base64( RSA* public_key, unsigned char* message, int msg_length );

void generate_xor_key();

int init_socket();

void send_message_to_client( char* message );
/*------------------------------------------------------------------------*/



/*---------------------------- VARIABLES ---------------------------------*/
#define PADDING RSA_PKCS1_PADDING
#define PORT_NUMBER 6666
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
  generate_xor_key();

  init_socket();

  receive_client_public_key();

  send_xor_key();

  send_xor_encrypted_message();

  close(server_socket);

  return 0;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int receive_client_public_key() 
{
  int bytes_read = recv( client_socket, str_public_key_client, 300, 0 );
  if( bytes_read ) printf( "\nClient Public Key: \n%s\n", str_public_key_client );
  return bytes_read;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
void send_xor_key()
{
  RSA* rsa_client_public_key = load_public_key_from_string( str_public_key_client  );

  char* rsa_encrypted_xor_key = rsa_encrypt_then_base64( rsa_client_public_key, xor_key, strlen(xor_key) );
  
  send_message_to_client( rsa_encrypted_xor_key );

  RSA_free( rsa_client_public_key );
  free( rsa_encrypted_xor_key ); 
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
void send_xor_encrypted_message()
{ 
  char* message = "this is a message from the server";
  
  int message_length = strlen( message );
  
  char* encrypted_xor_message = xor_message( message, message_length );

  send_message_to_client( encrypted_xor_message );

  free( encrypted_xor_message );
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
RSA* load_public_key_from_string( const char* public_key_string )
{
  BIO* bio = BIO_new_mem_buf( (void*)public_key_string, -1 ); 
  
  BIO_set_flags( bio, BIO_FLAGS_BASE64_NO_NL ); // NO NL
  
  // Load the RSA key from the BIO
  RSA* rsa_public_key = PEM_read_bio_RSA_PUBKEY( bio, NULL, NULL, NULL );
  
  if( !rsa_public_key )
    printf( "ERROR: Could not load PUBLIC KEY! PEM_read_bio_RSA_PUBKEY FAILED: %s\n", ERR_error_string( ERR_get_error(), NULL ) );
  
  BIO_free( bio );
  return rsa_public_key;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
/*
 * RSA_size( public_key ) is requried to determine how much memory must be allocated for the encrypted message.
 * You can only encrypt data as large as the RSA key length.
 */
unsigned char* rsa_encrypt( RSA* public_key, const unsigned char* msg, int msg_length, int* encrypted_msg_length )
{
  int rsa_key_length_in_bytes = RSA_size( public_key ) ;
  unsigned char* encrypted_message = (unsigned char*)malloc( rsa_key_length_in_bytes ) ;
  
  *encrypted_msg_length = RSA_public_encrypt( msg_length, (const unsigned char*)msg, encrypted_message, public_key, PADDING ) ;
  
  if( *encrypted_msg_length == -1 )
    printf("ERROR: RSA_public_encrypt: %s\n", ERR_error_string(ERR_get_error(), NULL));

  return encrypted_message ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
char* rsa_encrypt_then_base64( RSA* public_key, unsigned char* message, int msg_length )
{
  int rsa_encrypted_message_length;
  unsigned char* rsa_encrypted_message = rsa_encrypt( public_key, message, msg_length, &rsa_encrypted_message_length );
  
  int rsa_encrypted_message_base64_length;
  char* rsa_encrypted_message_base64 = base64( rsa_encrypted_message, rsa_encrypted_message_length, &rsa_encrypted_message_base64_length );
  
  free( rsa_encrypted_message ) ;
  
  return rsa_encrypted_message_base64;
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

  if(listen(server_socket,5)==0) printf("Server Is Listening...\n");
  else printf("Error: Server Is Not Listening\n");

  client_socket = accept(server_socket, NULL, NULL);
  if(client_socket > -1) printf("A client has connected to the server\n");
  else printf("Error: A client was not able to connect to the server\n");
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
/*
 * The function appends a new-line ('\n') character to the message because the client 
 * is using BufferedReader.readLine method which reads a line of text until the
 * terminator char which is '\n'.
 */
void send_message_to_client(char* message)
{
  char new_line = '\n';
  strncat(message, &new_line, 1); 

  int bytes_sent = write( client_socket, message, strlen(message) ) ;
  
  if(bytes_sent > 0)
  {
    printf("--> [%d bytes was sent]: %s\n", bytes_sent, message);
  }
  
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
