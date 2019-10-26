#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>


int main()
{
    char message_size_one[16];
    char message_size_two[16];
    char client_message[2000];

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

    /*recv(client_socket, client_message, strlen(client_message), 0);
    printf("Data received: %s",client_message);
    memset(client_message, 0, sizeof(client_message)); 

    recv(client_socket, client_message, strlen(client_message), 0);
    printf("Data received: %s",client_message);
    */
    

    recv(client_socket, message_size_one, strlen(message_size_one), 0);
    int i;
    printf("Data size: %s",message_size_one);
    //sscanf(message_size_one, "%d", &i);
    //recv_all(client_socket, i);

    recv(client_socket, message_size_two, strlen(message_size_two), 0);
    printf("Data size: %s",message_size_two);
    int j;
    //sscanf(message_size_two, "%d", &j);
    //recv_all(client_socket, j);


    char public_key_exponent[] = "1995\n";
    write(client_socket, public_key_exponent, strlen(public_key_exponent));

    char public_key_modulus[] = "24\n";
    write(client_socket, public_key_modulus, strlen(public_key_modulus));

    char resolutions[] = "640×480, 800×600, 960×720, 1024×768, 1280×960, 1400×1050, 1440×1080, 1600×1200, 1856×1392, 1920×1440, and 2048×1536\n";
    write(client_socket, resolutions, strlen(resolutions));

    close(server_socket);

    return 0;
}

//char *data_ptr = (char*) data;
int recv_all(int client_socket, int data_size)
{
    char buff[2000];
    char *data_ptr = buff;
    int bytes_recv;

    while (data_size > 0)
    {
        bytes_recv = recv(client_socket, data_ptr, data_size, 0);
        if (bytes_recv <= 0)
            return bytes_recv;

        data_ptr += bytes_recv;
        data_size -= bytes_recv;
    }
    printf("recv_all --> Data received: %s",buff);
    memset(buff, 0, sizeof(buff)); 

    return 1;
}

int read(int client_socket) {
    char buff[16];
    char *data_ptr = buff;
    int bytes_recv;
     
    bytes_recv = recv(client_socket, data_ptr, data_size, 0);
    while (data_size > 0)
    {
        bytes_recv = recv(client_socket, data_ptr, data_size, 0);
        if (bytes_recv <= 0)
            return bytes_recv;

        data_ptr += bytes_recv;
        data_size -= bytes_recv;
    }
    printf("recv_all --> Data received: %s",buff);
    memset(buff, 0, sizeof(buff)); 

    return 1;
}



