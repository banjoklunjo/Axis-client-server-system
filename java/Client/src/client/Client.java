package client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Client implements Runnable {
    private Controller controller;
    private Socket socket;
    private BufferedReader bufferedReader;
    private InputStreamReader inputStreamReader;
    private boolean online;
    private int length = 40000;
    private InputStream inputStream;
    private int realSize = 0;

    private byte[] imgBuf;


    public Client(Controller controller, Socket socket) {
        this.controller = controller;
        this.socket = socket;
    }

    private void readServerImage() {
        try {

            InputStream inputStream = socket.getInputStream();
            int length = inputStream.available();
            // byte[] message = readExactly(inputStream, length);
            byte[] message = new byte[length];

            String s = "";
            if (length > 2 && length < 20) {
                message = readExactly(inputStream, length);
                System.out.println("ska skriva ut storleken: ");
                s = new String(message);
                System.out.println(s);

                try {
                    realSize = Integer.valueOf(s.trim());
                    System.out.println("skriv ut realSize nuuu: ");
                    System.out.println(realSize);
                } catch (NumberFormatException e) {
                    System.out.println(e.toString());
                }

            }

            //
            else if (length > 20) {

                message = new byte[realSize];

                //int index = inputStream.read(message);
                BufferedInputStream stream = new BufferedInputStream(inputStream);
                // int index = inputStream.read(message);
                // System.out.println("Index:   " + index);

                // new
                for (int read = 0; read < realSize;) { // read until the byte array is filled
                    read += stream.read(message, read, message.length - read);
                }

                // System.out.println("Index:   " + length);
                ByteArrayInputStream bais = new ByteArrayInputStream(message);
                final BufferedImage bufferedImage = ImageIO.read(bais);

                if (bufferedImage != null) {
                    controller.updateImage(bufferedImage);
                }

            }

        } catch (IOException e) {
            System.out.println("readServerImage() --> Error = "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isNumeric(String number){
        try {
            Integer.parseInt(number);
        } catch(NumberFormatException | NullPointerException nullPointerException) {
            return false;
        }
        return true;
    }

    public byte[] readExactly(InputStream input, int size) throws IOException
    {
        byte[] data = new byte[size];
        int index = 0;
        while (index < size)
        {
            int bytesRead = input.read(data, index, size - index);
            if (bytesRead < 0)
            {
                throw new IOException("Insufficient data in stream");
            }
            index += size;
        }
        return data;
    }

    private String readServerMessage() {
        String message = null;
        try {
            message = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    public void sendToServer(String message) {
        PrintWriter output;
        try {
            output = new PrintWriter(socket.getOutputStream());
            output.println(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            online = true;
        } catch (IOException e1) {
            e1.printStackTrace();
            close();
        }
    }

    @Override
    public void run() {
        init();
        List<String> resolutions = getCameraResolutions();
        if (resolutions != null)
            controller.setResolutions(resolutions);
        while (online) {
            readServerImage();
        }
    }

    private List<String> getCameraResolutions() {
        String msgFromServer = readServerMessage();
        List<String> resolutions = null;
        if (msgFromServer != null) {
            resolutions = Arrays.asList(msgFromServer.split(","));
        }
        return resolutions;
    }

    public void close() {
        try {
            online = false;
            inputStreamReader.close();
            socket.close();
            controller.onDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}