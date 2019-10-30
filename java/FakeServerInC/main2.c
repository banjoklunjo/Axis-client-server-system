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



int main()
{
	int        rc;
	RSA*       rsa = RSA_new();
	BIGNUM*    bignum_e = BN_new();
	EVP_PKEY*  pkey;
	BIO*       pem1;

        size_t     pri_len;             // Length of private key
        size_t     pub_len;             // Length of public key
        char       *pri_key;            // Private key
        char       *pub_key;            // Public key
        char       msg[KEY_LENGTH/8];   // Message to encrypt
        char       *encrypt = NULL;     // Encrypted message
        char       *decrypt = NULL;     // Decrypted message
        char       *err;                // Buffer for any error messages
        char       char_pub_key_len[5];

        // To get the C-string PEM form:
        BIO *pri = BIO_new(BIO_s_mem());
        BIO *pub = BIO_new(BIO_s_mem());

	int size = 0;

        // set public exponent e to 65537 (RSA_F4 = 65537)
	rc = BN_set_word(bignum_e, RSA_F4);
	if(rc != 1) {
	    //error message
	}

        // generates the keypair with KEY_LENGTH with the public exponent bignum_e (in this case 65537)
	rc = RSA_generate_key_ex(rsa, KEY_LENGTH, bignum_e, NULL);
	if(rc != 1) {
	    //error message
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
  
        printf("Public Key Length: %s\n", char_pub_key_len);
    /* --------------------------- OPEN SSL END --------------------------- */



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
    
    //write(client_socket, char_pub_key_len, strlen(char_pub_key_len));
    //write(client_socket, "\n", strlen("\n"));

    int sent_all_data = send_all(client_socket, pub_key, pub_len); 
    if(sent_all_data) printf("Public key successfully sent\n");
    else printf("Failed to send the Public key\n");
    //write(client_socket, pub_key, strlen(pub_len_char_array));

    close(server_socket);
    /* --------------------------- SOCKETS END --------------------------- */


    //IO_free(pem1);
    EVP_PKEY_free(pkey);
    RSA_free(rsa); 
    BN_free(bignum_e);

    return 0;
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

