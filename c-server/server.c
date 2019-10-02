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
#include <capture.h>
#include "cam_server.h"

#define _GNU_SOURCE


int main(void){

	start_up_server();
	return 0;
}


