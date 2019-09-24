package client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class Client implements Runnable {
    private Controller controller;
    private Socket socket;
    private BufferedReader buffReader;
    private InputStreamReader input;
    private boolean online;


    public Client(Controller controller, Socket socket) {
        this.controller = controller;
        this.socket = socket;
    }

    private void readServerImage() {
        try {
            int length = socket.getInputStream().available();
            if(length > 0) {
                System.out.println("the lenth of the received message" + length);
            }

            BufferedImage bufferedImage = ImageIO.read(socket.getInputStream());
            if (bufferedImage != null)
                controller.updateImage(bufferedImage);
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
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
            message = buffReader.readLine();
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
            input = new InputStreamReader(socket.getInputStream());
            buffReader = new BufferedReader(input);
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
            input.close();
            socket.close();
            controller.onDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}