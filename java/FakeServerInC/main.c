#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <math.h>



/* protoypes */
void generate_rsa_key(void);
int gcd(int a, int h);
double gen_key(void);

typedef struct rsa 
{
  double e;
  double n;
  double d;
} rsa_variables;
/* ---------- */




/* Variables */
char public_key_client_modulus[20];
char public_key_client_exponent[20];
char public_key_server_modulus[20];
char public_key_server_exponent[20];
char char_xor_key[20];
char client_message[2000];
rsa_variables rsa_variables_server;
double xor_key; 
/* --------- */



int main()
{

    generate_rsa_key();
    xor_key = 5.0;//gen_key();
    /*double c = pow(xor_key, rsa_variables_server.e); //encrypt the message
    double m = pow(c, rsa_variables_server.d);
    c=fmod(c, rsa_variables_server.n);
    m=fmod(m, rsa_variables_server.n);
    printf("Original message = %f \n", xor_key);
    printf("Encrypted message c = %f \n", c);
    printf("Decrypted message m = %f \n", m);   
    */
    printf("Server e = %f \n", rsa_variables_server.e);
    printf("Server n = %f \n", rsa_variables_server.n);
    printf("Server d = %f \n", rsa_variables_server.d);
    printf("Server xor key = %f \n", xor_key);

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

    
  
    /* -------------- READ CLIENT PUBLIC KEY -------------- */
    int bytes_read = 0;
    bytes_read = recv(client_socket, public_key_client_modulus, 20, 0);
    printf("Recevied -> Public Key Client Modulus: %s", public_key_client_modulus);

    bytes_read = recv(client_socket, public_key_client_exponent, 20, 0);
    printf("Recevied -> Public Key Client Exponent: %s", public_key_client_exponent);

    /* ---------------------------------------------------- */


    /* -------------- SEND SERVER PUBLIC KEY -------------- */
    //snprintf(public_key_server_exponent, 20, "%f", rsa_variables_server.e);
    gcvt(rsa_variables_server.e, 20, public_key_server_exponent);
    printf("Sending -> Public Key Server Exponent: %s \n", public_key_server_exponent);
    write(client_socket, public_key_server_exponent, strlen(public_key_server_exponent));
    write(client_socket, "\n", strlen("\n"));

    //snprintf(public_key_server_modulus, 20, "%f", rsa_variables_server.n);
    gcvt(rsa_variables_server.n, 20, public_key_server_modulus);
    printf("Sending -> Public Key Server Modulus: %s \n", public_key_server_modulus);
    write(client_socket, public_key_server_modulus, strlen(public_key_server_modulus));
    write(client_socket, "\n", strlen("\n"));
    /* ---------------------------------------------------- */


    /* ----------- SEND XOR KEY ----------- */
    double encrypted_xor = fmod( (pow(xor_key,rsa_variables_server.e)), rsa_variables_server.n);
    printf("Encrypted xor = %f \n", encrypted_xor);
    char encrypted_char_xor_key[20];
    gcvt(encrypted_xor, 20, encrypted_char_xor_key);
    printf("Sending -> Encrypted XOR key: %s \n", encrypted_char_xor_key);
    write(client_socket, encrypted_char_xor_key, strlen(encrypted_char_xor_key));
    write(client_socket, "\n", strlen("\n"));
    /* ------------------------------------ */


    /* -------------- SEND RESOLUTIONS -------------- */
    char resolutions[] = "640×480, 800×600, 960×720, 1024×768, 1280×960, 1400×1050, 1440×1080, 1600×1200, 1856×1392, 1920×1440, and 2048×1536\n";
    write(client_socket, resolutions, strlen(resolutions));
    /* ---------------------------------------------- */


    close(server_socket);


    return 0;
}

void generate_rsa_key(void) {
    double p = 187787;
    double q = 189019;
    rsa_variables_server.n = p * q;
    double phi = ( p-1 ) * ( q-1 );
    double e = 7;
    double track;
    //for checking that 1 < e < phi(n) and gcd(e, phi(n)) = 1; i.e., e and phi(n) are coprime.
    while(e<phi) {
      track = gcd(e,phi);
      if(track==1)
         break;
      else
         e++;
    }
    double d1 = 1/e;
    double d = fmod(d1,phi);
    rsa_variables_server.d = d;
    rsa_variables_server.e = e;
}

double gen_key(void) {
    return (rand()%401)+100;
}

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




