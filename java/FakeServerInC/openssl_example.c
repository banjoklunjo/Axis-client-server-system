#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <openssl/rsa.h>
#include <openssl/engine.h>
#include <openssl/pem.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "base64.h"

/* prototypes in order to avoid implicit declaration warning */
void test_function_send_encrypted_message_to_client();
void test_function_recieve_encrypted_message_and_decrypt_it();
void generate_xor_key();

#define RSA_KEY_SIZE  1024
#define PADDING RSA_PKCS1_PADDING
#define PORT_NUMBER 6666
int server_socket;
int client_socket;
RSA* rsa;
EVP_PKEY* pkey; /* structure for holding public and private key [EVP stands for envelope encryption] */
char* str_private_key;
char* str_public_key;
char str_public_key_client[271] ;
char client_message[300];
char xor_key[8];

/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
unsigned char* makeAlphaString( int dataSize )
{
  unsigned char* s = (unsigned char*) malloc( dataSize ) ;
  
  int i;
  for( i = 0 ; i < dataSize ; i++ )
    s[i] = 65 + i ;
  s[i-1]=0;//NULL TERMINATOR ;)
  
  return s ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
RSA* loadPublicKeyFromString( const char* publicKeyStr )
{
  BIO* bio = BIO_new_mem_buf( (void*)publicKeyStr, -1 ) ; 
  
  BIO_set_flags( bio, BIO_FLAGS_BASE64_NO_NL ) ; // NO NL
  
  // Load the RSA key from the BIO
  RSA* rsaPubKey = PEM_read_bio_RSA_PUBKEY( bio, NULL, NULL, NULL ) ;
  if( !rsaPubKey )
    printf( "ERROR: Could not load PUBLIC KEY!  PEM_read_bio_RSA_PUBKEY FAILED: %s\n", ERR_error_string( ERR_get_error(), NULL ) ) ;
  
  BIO_free( bio ) ;
  return rsaPubKey ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
RSA* loadPRIVATEKeyFromString( const char* privateKeyStr )
{
  BIO *bio = BIO_new_mem_buf( (void*)privateKeyStr, -1 );
  //BIO_set_flags( bio, BIO_FLAGS_BASE64_NO_NL ) ; // NO NL
  RSA* rsaPrivKey = PEM_read_bio_RSAPrivateKey( bio, NULL, NULL, NULL ) ;
  
  if ( !rsaPrivKey )
    printf("ERROR: Could not load PRIVATE KEY!  PEM_read_bio_RSAPrivateKey FAILED: %s\n", ERR_error_string(ERR_get_error(), NULL));
  
  BIO_free( bio ) ;
  return rsaPrivKey ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
unsigned char* rsaDecrypt( RSA *privKey, const unsigned char* encryptedData, int *resultLen )
{
  int rsaLen = RSA_size( privKey ) ; // That's how many bytes the decrypted data would be
  
  unsigned char *decryptedBin = (unsigned char*)malloc( rsaLen ) ;
  *resultLen = RSA_private_decrypt( RSA_size(privKey), encryptedData, decryptedBin, privKey, PADDING ) ;
  if( *resultLen == -1 )
    printf( "ERROR: RSA_private_decrypt: %s\n", ERR_error_string(ERR_get_error(), NULL) ) ;
    
  return decryptedBin ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
unsigned char* rsaEncrypt( RSA *pubKey, const unsigned char* str, int dataSize, int *resultLen )
{
  int rsaLen = RSA_size( pubKey ) ;
  unsigned char* ed = (unsigned char*)malloc( rsaLen ) ;
  
  // RSA_public_encrypt() returns the size of the encrypted data
  // (i.e., RSA_size(rsa)). RSA_private_decrypt() 
  // returns the size of the recovered plaintext.
  *resultLen = RSA_public_encrypt( dataSize, (const unsigned char*)str, ed, pubKey, PADDING ) ; 
  if( *resultLen == -1 )
    printf("ERROR: RSA_public_encrypt: %s\n", ERR_error_string(ERR_get_error(), NULL));

  return ed ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
// You may need to encrypt several blocks of binary data (each has a maximum size
// limited by pubKey).  You shoudn't try to encrypt more than
// RSA_LEN( pubKey ) bytes into some packet.
char* rsaEncryptThenBase64( RSA *pubKey, unsigned char* binaryData, int binaryDataLen, int *outLen )
{
  int encryptedDataLen ;
  
  // RSA encryption with public key
  unsigned char* encrypted = rsaEncrypt( pubKey, binaryData, binaryDataLen, &encryptedDataLen ) ;
  
  // To base 64
  int asciiBase64EncLen ;
  char* asciiBase64Enc = base64( encrypted, encryptedDataLen, &asciiBase64EncLen ) ;
  
  // Destroy the encrypted data (we are using the base64 version of it)
  free( encrypted ) ;
  
  // Return the base64 version of the encrypted data
  return asciiBase64Enc ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
unsigned char* rsaDecryptThisBase64( RSA *privKey, char* base64String, int *outLen )
{
  int encBinLen ;
  unsigned char* encBin = unbase64( base64String, (int)strlen( base64String ), &encBinLen ) ;
  
  // rsaDecrypt assumes length of encBin based on privKey
  unsigned char *decryptedBin = rsaDecrypt( privKey, encBin, outLen ) ;
  free( encBin ) ;
  
  return decryptedBin ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int initSocket() 
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
int generateRSAKeyPair() 
{
  int return_value;
  // To get the C-string PEM form:
  BIO *bio_private = BIO_new( BIO_s_mem() ) ;
  BIO *bio_public = BIO_new( BIO_s_mem() ) ;
  
  // set BIGNUM to 65537 (RSA_F4 = 65537)
  // = Public Key Exponent ( e ) 
  BIGNUM* bignum_e = BN_new() ;
  return_value = BN_set_word( bignum_e, RSA_F4 ) ; 
  if(return_value != 1) printf( "Error: Could not set value to bignum_e\n" ) ;

  // generate rsa keypair
  rsa = RSA_new() ;
  return_value = RSA_generate_key_ex(rsa, RSA_KEY_SIZE, bignum_e, NULL);
  if(return_value != 1) printf( "Error: Could not generate a key pair\n" ) ;
  else printf("Generating RSA (%d bits) keypair...\n", RSA_KEY_SIZE);

  pkey = EVP_PKEY_new();
  return_value = EVP_PKEY_set1_RSA(pkey, rsa);
  if(return_value != 1) printf( "Error: EVP_PKEY_set1_RSA\n" ) ;

  PEM_write_bio_PrivateKey(bio_private, pkey, NULL, NULL, 0, NULL, NULL) ; /* save private key */
  PEM_write_bio_PUBKEY(bio_public, pkey) ; /* save public key */


  size_t private_key_length = BIO_pending(bio_private);             
  size_t public_key_length = BIO_pending(bio_public);

  str_private_key = malloc(private_key_length + 1);
  str_public_key = malloc(public_key_length + 1);

  BIO_read(bio_private, str_private_key, private_key_length);
  BIO_read(bio_public, str_public_key, public_key_length);              

  str_private_key[private_key_length] = '\0';
  str_public_key[public_key_length] = '\0';

  printf("\n%s\n%s\n", str_private_key, str_public_key);

  // avoid memory leaks by freeing shit
  BIO_free_all(bio_public);
  BIO_free_all(bio_private);
  BN_free(bignum_e);

  return 1;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int send_all(int socket, void *buffer, size_t length)
{
    char *ptr = (char*) buffer;
    while (length > 0)
    {
        int i = write(socket, ptr, length);
        printf("Following frame was sent to the client: %s", ptr);
        if (i < 1) return 0;
        ptr += i;
        length -= i;
    }
    return 1;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int send_server_public_key()
{
  int sent_all_data = write(client_socket, str_public_key, strlen(str_public_key)); 
  if(sent_all_data) printf("\nServer Public Key Was Sent To The Client\n");
  else printf("\nError: Server Public Key Was Not Sent To The Client\n");
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int receive_client_public_key() 
{
  int bytes_read = recv(client_socket, str_public_key_client, 271 , 0);
  if(bytes_read) printf("\nReceived -> Client Public Key: \n%s\n", str_public_key_client);
  return 1;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
int main( int argc, const char* argv[] )
{
  generate_xor_key();

  generateRSAKeyPair();

  initSocket();

  send_server_public_key();

  receive_client_public_key();

  // --------------- TEST FUNCTIONS ---------------
  test_function_send_encrypted_message_to_client();
  test_function_recieve_encrypted_message_and_decrypt_it();
  // ----------------------------------------------
  
  close(server_socket);
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
/* rand() returns pseudo-random numbers. It generates numbers based on a given algorithm. 
The starting point of that algorithm is always the same, so you'll see the same sequence 
generated for each invocation.
You can set the "seed" of the random generator with the srand function(only call srand once in a program). 
One common way to get different sequences from the rand() generator is to set the seed to the current time 
https://stackoverflow.com/questions/42570700/generating-10-random-characters */
void generate_xor_key()
{
  srand(time(NULL));
  int i; 
  char abc[26]="abcdefghijklmnopqrstuvwxyz";
  for (i = 0; i < 8; ++i) {
	xor_key[i] = abc[rand() % (sizeof(abc) - 1)];
        printf("%c", xor_key[i]);
  }
  xor_key[8] = 0;
}

// ------------------------------------------------------------------------
// ---------------------------- TEST FUNCTIONS ----------------------------
// ------------------------------------------------------------------------
void test_function_send_encrypted_message_to_client()
{
  RSA *rsa_client_public_key = loadPublicKeyFromString( str_public_key_client  ) ;

  int asciiB64ELen;

  char* asciiB64E = rsaEncryptThenBase64( rsa_client_public_key, xor_key, sizeof(xor_key), &asciiB64ELen ) ;
  printf( "Sending base64_encoded message:\n%s\n", asciiB64E ) ;

  write(client_socket, asciiB64E, 172);
  write(client_socket, "\n", sizeof("\n"));

  RSA_free( rsa_client_public_key ) ; // free the public key when you are done all your encryption
  free( asciiB64E ) ;
}
/*------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
void test_function_recieve_encrypted_message_and_decrypt_it()
{ 
  // RECEIVE ENCRYPED MESSAGE FROM CLIENT AND DECRYPT IT
  while ( 1 ) {
    ssize_t nb = recv(client_socket, client_message, 300, 0);
    if ( nb == -1 ) err( "recv failed" );
    if ( nb == 0 ) break; /* got end-of-stream */
  }  

  printf( "Received Encrypted Message From Client: %s", client_message ) ;  
   
  RSA* rsa_server_private_key = loadPRIVATEKeyFromString( str_private_key ) ;
  int rBinLen ;
  unsigned char* rBin = rsaDecryptThisBase64( rsa_server_private_key, client_message, &rBinLen ) ;
  printf("Decrypted %d bytes, the recovered data is:\n%.*s\n\n", rBinLen, rBinLen, rBin ) ; // rBin is not necessarily NULL

  RSA_free( rsa_server_private_key ) ;
  free( rBin ) ;
}

/*
void ReadXBytes(int socket, unsigned int x, void* buffer)
{
    int bytesRead = 0;
    int result;
    while (bytesRead < x)
    {
        result = read(socket, buffer + bytesRead, x - bytesRead);
        if (result < 1 )
        {
            // Throw your error.
        }

        bytesRead += result;
    }
}*/
