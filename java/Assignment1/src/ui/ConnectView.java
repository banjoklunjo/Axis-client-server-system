package ui;

import java.util.List;

import client.Controller;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import model.Camera;
import model.Network;

public class ConnectView {
	private Controller controller;

	@FXML
	private Label statusLabel;

	@FXML
	private Label sentLabel;

	@FXML
	private Button btnConnect, btnSend;

	@FXML
	private TextField ipField, frequencyField, portField;

	@FXML
	private ListView<String> listView;

	@FXML
	public void onConnectClick() {
		controller.connect(new Network(ipField.getText().trim(), portField.getText().trim()));
	}

	public void onConnected() {
		ipField.setDisable(true);
		portField.setDisable(true);
		btnConnect.setDisable(true);
		statusLabel.setText("Connected to IP: " + ipField.getText() + " at Port: " + portField.getText());
	}

	public void setResolutionsInListView(List<String> resolutions) {
		listView.setItems(FXCollections.observableList(resolutions));
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}

	public void setStatusText(String text) {
		statusLabel.setText(text);
	}

	@FXML
	public void onSend() {
		String resolution = listView.getSelectionModel().getSelectedItem();
		String frequency = frequencyField.getText();
		controller.sendCameraParameters(new Camera(resolution, frequency));
	}

	public void onDisconnect() {
		if (listView.getItems() != null) {
			listView.setItems(null);
		}
		ipField.setDisable(false);
		portField.setDisable(false);
		btnConnect.setDisable(false);
		ipField.setText("");
		portField.setText("");
		statusLabel.setText("Disconnected from the server");
	}

	public void invalidNetworkParameters() {
		statusLabel.setText("Invalid IP or/and PORT");
	}

	public void setController(Controller controller) {
		ipField.setText("localhost");
		portField.setText("6666");
		this.controller = controller;
	}

	public void setSentText(String text) {
		sentLabel.setText(text);
	}

	public void disableSendButton() {
		btnSend.setDisable(true);
	}

}
