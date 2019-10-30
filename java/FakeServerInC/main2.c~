#include <stdio.h>
#include <stdlib.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <openssl/x509.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/err.h>
#include <string.h>

#define KEY_LENGTH  1024
#define PUB_EXP     65537
#define PRINT_KEYS

int send_all(int socket, void *buffer, size_t length);
EVP_PKEY* loadPUBLICKeyFromString( const char* publicKeyStr );


EVP_PKEY*  pkey;
char       *pub_key;            // Public key
size_t     pri_len;             // Length of private key
size_t     pub_len;             // Length of public key
RSA*       rsa;
BIGNUM*    bignum_e;

RSA*       rsa_client;
char       client_message_pub_key[2000];



int main()
{
    setupRSA();

    /* --------------------------- SOCKETS START --------------------------- */
    int server_socket;
    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    struct sockaddr_in server_address;
    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(6666);
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);

    bind(server_socket, (struct sockaddr *) &server_address, sizeof(server_address));

    if(listen(server_socket,5)==0)
	printf("Listening\n");
    else
	printf("Not Listening\n");

    int client_socket;
    client_socket = accept(server_socket, NULL, NULL);


    int sent_all_data = send_all(client_socket, pub_key, pub_len); 
    if(sent_all_data) printf("Public key successfully sent\n");
    else printf("Failed to send the Public key\n");
    
    int bytes_read = recv(client_socket , client_message_pub_key , 2000 , 0);
    if(bytes_read) printf("Bytes read OK\n");
    else printf("Bytes read FAIL\n");
    printf("Client message public key: %s\n", client_message_pub_key);
    loadPUBLICKeyFromString(client_message_pub_key);
    close(server_socket);
    /* --------------------------- SOCKETS END --------------------------- */


    //IO_free(pem1);
    EVP_PKEY_free(pkey);
    RSA_free(rsa); 
    BN_free(bignum_e);

    return 0;
}

int setupRSA() {
        int        rc;
	rsa        = RSA_new();
	bignum_e   = BN_new();
	BIO*       pem1;

        char       *pri_key;            // Private key
        char       msg[KEY_LENGTH/8];   // Message to encrypt
        char       *encrypt = NULL;     // Encrypted message
        char       *decrypt = NULL;     // Decrypted message
        char       *err;                // Buffer for any error messages
        char       char_pub_key_len[5];


        // To get the C-string PEM form:
        BIO *pri = BIO_new(BIO_s_mem());
        BIO *pub = BIO_new(BIO_s_mem());

        // set public exponent e to 65537 (RSA_F4 = 65537)
	rc = BN_set_word(bignum_e, RSA_F4);
	if(rc != 1) {
	    printf("Error: Could not set Public Exponent to 65537");
	}

        // generates the keypair with KEY_LENGTH with the public exponent bignum_e (in this case 65537)
	rc = RSA_generate_key_ex(rsa, KEY_LENGTH, bignum_e, NULL);
	if(rc != 1) {
	    printf("Error: Could not generate a key pair");
	} else {
           printf("Generating RSA (%d bits) keypair...", KEY_LENGTH);
        }

        // EVP structure containing public and private key
	pkey = EVP_PKEY_new();
	rc = EVP_PKEY_set1_RSA(pkey, rsa);
	if(rc != 1) {
	    //error message
	}
     
	rc = PEM_write_bio_PrivateKey(pri, pkey, NULL, NULL, 0, NULL, NULL);
        rc = PEM_write_bio_PUBKEY(pub, pkey);
        
        pri_len = BIO_pending(pri);
        pub_len = BIO_pending(pub);

        pri_key = malloc(pri_len + 1);
        pub_key = malloc(pub_len + 1);

        BIO_read(pri, pri_key, pri_len);
        BIO_read(pub, pub_key, pub_len);

        pri_key[pri_len] = '\0';
        pub_key[pub_len] = '\0';
        
        snprintf(char_pub_key_len, sizeof char_pub_key_len, "%zu", pub_len);

        printf("\n%s\n%s\n", pri_key, pub_key);
  
        printf("Public Key Length (Including Headers): %s\n", char_pub_key_len);
}

EVP_PKEY* loadPUBLICKeyFromString( const char* publicKeyStr )
{
  // A BIO is an I/O abstraction (Byte I/O?)
  
  // BIO_new_mem_buf: Create a read-only bio buf with data
  // in string passed. -1 means string is null terminated,
  // so BIO_new_mem_buf can find the dataLen itself.
  // Since BIO_new_mem_buf will be READ ONLY, it's fine that publicKeyStr is const.
  BIO* bio = BIO_new_mem_buf( (void*)publicKeyStr, -1 ) ; // -1: assume string is null terminated
  EVP_PKEY*  pkey_client;
  //BIO_set_flags( bio, BIO_FLAGS_BASE64_NO_NL ) ; // NO NL
  
  // Load the RSA key from the BIO
  pkey_client = PEM_read_bio_PUBKEY( bio, NULL, NULL, NULL ) ;
  if( !rsa_client )
    printf( "ERROR: Could not load PUBLIC KEY!  PEM_read_bio_RSA_PUBKEY FAILED: %s\n", ERR_error_string( ERR_get_error(), NULL ) ) ;
  
  BIO_free( bio ) ;
  return pkey_client ;
}


int send_all(int socket, void *buffer, size_t length)
{
    char *ptr = (char*) buffer;
    while (length > 0)
    {
        int i = write(socket, ptr, length);
        printf("Following frame was sent to client: %s", ptr);
        if (i < 1) return 0;
        ptr += i;
        length -= i;
    }
    return 1;
}

