package client;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChattingFrame extends JFrame{
	
	private Client client;
	private JTextArea ta;
	
	public ChattingFrame() {
		client = new Client(this, "127.0.0.1", 9999);
		
		if(client.connectToServer()) {
			guiInit();
		}
		else {
			System.exit(0);
		}
	}
	
	private void guiInit() {
		setTitle("채팅 프로그램");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500,600);
		setResizable(false);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		
		ta = new JTextArea();
		ta.setEditable(false);
		add(ta, BorderLayout.CENTER);
		JTextField tf = new JTextField();
		tf.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					client.sendMessageToServer("Chat/" + tf.getText());
					tf.setText("");
				}
			}
		});
		add(tf, BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	public void updateChat(String text) {
		String oldText = ta.getText();
		
		if(oldText.isEmpty()) {
			ta.setText(text);
		}
		else {
			ta.setText(oldText + "\n" + text);
		}
	}
}
