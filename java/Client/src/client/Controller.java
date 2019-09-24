package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javafx.application.Platform;
import model.Camera;
import model.Network;
import ui.ConnectView;
import ui.PictureView;

public class Controller {
    private ConnectView connectView;
    private PictureView pictureView;
    private ExecutorService executor;
    private Socket socket;
    private Client client;

    public Controller(ExecutorService executor, ConnectView connectView) {
        this.executor = executor;
        this.connectView = connectView;
        connectView.setController(this);
    }

    public void connect(Network network) {
        if (network.isValidFormat()) {

            // createSocket needs to be in another thread, otherwise it will block the main thread and make the UI unresponsive
            // the get() in executor.submit waits until the socket has been created, otherwise it would just continue
            // executing the upcoming code.
            try {
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        socket = createSocket(network);
                    }

                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                connectView.onConnected();
                client = new Client(this, socket);
                executor.execute(client);
            }
        } else {
            connectView.invalidNetworkParameters();
        }
    }

    public void updateImage(BufferedImage bufferedImage) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (pictureView == null) { // first instance
                    displayPictureView();
                } else if (pictureView.isWindowClosed()) { // closed
                    displayPictureView();
                    pictureView.setWindowsIsOpen();
                }
                pictureView.updateImage(bufferedImage);
            }
        });
    }

    public void sendCameraParameters(Camera camera) {
        if (camera.validParameters()) {
            client.sendToServer(camera.formatParametersForServer());
            connectView.setSentText(camera.formatParametersForServer());
            connectView.disableSendButton();
        } else {
            connectView.setSentText(camera.invalidFrequencyOrResolution());
        }
    }

    public void setResolutions(List<String> resolutions) {
        connectView.setResolutionsInListView(resolutions);
    }

    public void displayPictureView() {
        setPictureView(PictureView.displayPictureView());
    }

    public void onDisconnect() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                connectView.onDisconnect();
            }
        });

    }

    public void onQuit() {
        if (client != null)
            client.close();
        if (executor != null)
            executor.shutdown();
    }

    private Socket createSocket(Network network) {
        Socket tempSocket = null;
        try {
            tempSocket = new Socket(network.getIp(), network.getPort());
        } catch (IOException e) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    connectView.setStatusText(network.connectionFailedLog());
                }
            });

        }
        return tempSocket;
    }

    public void setPictureView(PictureView picView) {
        this.pictureView = picView;
        this.pictureView.setController(this);
    }

}