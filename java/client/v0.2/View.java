package ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import client.IController;

public class View implements ActionListener, IView {
	private JFrame frame;
	private JTextField ipTextField;
	private JTextField portTextField;
	private JButton btnConnect;
	private JTextField sendTextField;
	private JButton btnSend;
	private JTextArea txtAreaLog;
	private IController controller;

	
	public View(IController controller) {
		this.controller = controller;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnConnect) {
			String port = portTextField.getText();
			String ip = ipTextField.getText();
			controller.connect(ip, port);
		} else if (event.getSource() == btnSend) {
			controller.sendMessage(sendTextField.getText());
		}
	}

	@Override
	public void onConnect() {
		ipTextField.setEnabled(false);
		portTextField.setEnabled(false);
		btnConnect.setEnabled(false);
		btnSend.setEnabled(true);
		sendTextField.setEnabled(true);
		btnConnect.setText("Connected");
		setLogText("Connected to the server" + "\n" + "..." + "\n" + "..." + "\n" + "...");
	}

	@Override
	public void onDisconnect() {
		setLogText("Server is down");
	}

	@Override
	public void onEmptyFields() {
		setLogText("No value for IP or PORT set");
	}

	@Override
	public void onMessageSent(String message) {
		setLogText("Message sent to server -> " + message + "\n");
	}

	@Override
	public void onMessageReceived(String message) {
		setLogText(message);
	}
	
	public void setLogText(String message) {
		txtAreaLog.setText(txtAreaLog.getText() + "\n" + message);
	}

	public void display() {
		frame = new JFrame();
		frame.setBounds(100, 100, 300, 420);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setTitle("Client");
		initView();
		frame.setVisible(true);
		frame.setResizable(false);
	}

	private void initView() {
		initIpField();
		initPortField();
		initConnectBtn();
		initSend();
		initLogArea();
		initWindowExitListener();
	}

	private void initIpField() {
		ipTextField = new JTextField();
		ipTextField.setBounds(13, 13, 110, 25);
		ipTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (ipTextField.getText().length() >= 15)
					e.consume();
			}
		});
		frame.add(ipTextField);
	}

	private void initPortField() {
		portTextField = new JTextField();
		portTextField.setBounds(13, 40, 50, 25);
		portTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (portTextField.getText().length() >= 5)
					e.consume();
			}
		});
		frame.add(portTextField);
	}

	private void initConnectBtn() {
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(13, 70, 100, 23);
		frame.add(btnConnect);
		btnConnect.addActionListener(this);
	}

	private void initSend() {
		sendTextField = new JTextField();
		sendTextField.setBounds(13, 110, 250, 25);
		sendTextField.setEnabled(false);
		frame.add(sendTextField);
		btnSend = new JButton("Send Message");
		btnSend.setBounds(13, 140, 150, 23);
		btnSend.setEnabled(false);
		btnSend.addActionListener(this);
		frame.add(btnSend);
	}

	private void initLogArea() {
		txtAreaLog = new JTextArea();
		txtAreaLog.setEditable(false);
		JScrollPane pane = new JScrollPane(txtAreaLog);
		pane.setBounds(5, 170, 280, 200);
		pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		frame.add(pane);
	}

	private void initWindowExitListener() {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controller.onWindowExit();
				e.getWindow().dispose();
			}
		});
	}


}