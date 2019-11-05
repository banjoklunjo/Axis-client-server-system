PROGRAM	= server

CFLAGS+	= -Wall -g -O2
CFLAGS-y   += -W \
		-Wformat=2 \
	    -Wpointer-arith \
	    -Wbad-function-cast \
	    -Wstrict-prototypes \
	    -Wmissing-prototypes \
	    -Winline \
	    -Wdisabled-optimization \
	    -Wfloat-equal \
	    -Wall \
	    -Werror

LDFLAGS += -lcrypto -lssl 

$(PROGRAM): server.c
	$(CC) $(CFLAGS) $^ $(LDFLAGS) -o $@

cflags:
	$(info $(CFLAGS))

ldflags:
	$(info $(LDFLAGS))

clean:
	rm -f $(PROGS) *.o core


#openssl_example: server.c
#	$(CC) -o server server.c -lcrypto -lssl -I.
