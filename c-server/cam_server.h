#ifndef __CAM_SERVER_H__
#define __CAM_SERVER_H__

int start_up_server(void);
void* socketThread(void*);
double* generate_pub_key();
int gcd(int a, int h);
//double encryptXOR(double eValue, double nValue, double xorKey);
//double decryptXOR(double eValue, double nValue, double xorKey);
char *encrypt_char(char *message, char* key);
#endif /* CAM_SERVER */

