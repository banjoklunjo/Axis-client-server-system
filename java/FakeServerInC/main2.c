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
	int rc;
	RSA* rsa = RSA_new();
	BIGNUM* bignum_e = BN_new();
	EVP_PKEY* pub_key;
        EVP_PKEY* pri_key;
	BIO* bio_public;
        BIO* bio_private;

	bio_public = BIO_new_fp(stdout, BIO_NOCLOSE);
        bio_private = BIO_new_fp(stdout, BIO_NOCLOSE);
	char * string = (char*)malloc(600*sizeof(char)); //bigger than I need
	setbuf(stdout, string);
	int size = 0;

        // set public exponent e to 65537 (RSA_F4 = 65537)
	rc = BN_set_word(bignum_e,RSA_F4);
	if(rc != 1) {
	    //error message
	}

        // generates the keypair with 2048-bits with the public exponent bignum_e (in this case 65537)
	rc = RSA_generate_key_ex(rsa, 2048, bignum_e, NULL);
	if(rc != 1) {
	    //error message
	 }

	pub_key = EVP_PKEY_new();
	rc = EVP_PKEY_set1_RSA(pub_key,rsa);
	if(rc != 1) {
	    //error message
	}

        pri_key = EVP_PKEY_new();
	rc = EVP_PKEY_set1_RSA(pri_key,rsa);
	if(rc != 1) {
	    //error message
	}

	rc = PEM_write_bio_PrivateKey(bio_private, pri_key, NULL, NULL, 0, NULL, NULL);
        rc = PEM_write_bio_PUBKEY(bio_public,pub_key);
	size = strlen(string);
	setbuf(stdout, NULL);
        
	int p_size = EVP_PKEY_size(pub_key);
        char str[10];
        sprintf(str, "%d", p_size);
	printf("Public Key Length: %s\n", str);
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

    int sent_all_data = send_all(client_socket, string, strlen(string)); 
    if(sent_all_data) printf("Public key successfully sent\n");
    else printf("Failed to send the Public key\n");
    //write(client_socket, pub_key, strlen(pub_len_char_array));

    close(server_socket);
    /* --------------------------- SOCKETS END --------------------------- */


    //IO_free(pem1);
	EVP_PKEY_free(pub_key);
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

