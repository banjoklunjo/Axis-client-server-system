#ifndef __CAM_SERVER_H__
#define __CAM_SERVER_H__

#define SHARED_LIB_OK 0


/** Start up the server and start listening */
int start_up_server(void);

int shared_lib_function(void);

void * socketThread(void);

#endif /* CAM_SERVER */

