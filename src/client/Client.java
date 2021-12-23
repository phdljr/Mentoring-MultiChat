package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class Client {
	private ChattingFrame cf;
	
	private String ip;
	private int port;
	
	private Socket socket;
	private InputStream is;
	private DataInputStream dis;
	private OutputStream os;
	private DataOutputStream dos;
	
	public Client(ChattingFrame cf, String ip, int port) {
		this.cf = cf;
		this.ip = ip;
		this.port = port;
	}

	public boolean connectToServer() {
		try {
			socket = new Socket(ip, port);

			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);

			System.out.println(ip + ":" + port + "주소로 연결 완료");
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "알 수 없는 서버 주소", "오류", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "서버 접속 실패", "오류", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// 연결이 정상적으로 되면 서버로부터 메세지를 받는 스레드를 실행함
		if (socket != null) {
			runThreadToGetMessage();
		}

		return (socket != null);
	}
	
	private void runThreadToGetMessage() {
		new Thread(() -> {
			while (true) {
				try {
					String msg = dis.readUTF();
					inMessageFromServer(msg);
				} catch (IOException e) {
					disconnect();
					System.exit(0);
				}
			}
		}).start();
	}
	
	public void sendMessageToServer(String msg) {
		try {
			dos.writeUTF(msg);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "서버로 메세지 전달 오류", "오류", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void inMessageFromServer(String msg) {
		System.out.println("서버로부터 받은 메세지 : " + msg);

		StringTokenizer st = new StringTokenizer(msg, "/");

		String protocol = st.nextToken();
		String data = st.nextToken();
		
		System.out.println("프로토콜 : "+protocol);
		System.out.println("내용 : "+data);
		
		if(protocol.equals("Chat")) {
			cf.updateChat(data);
		}
	}
	
	public void disconnect() {
		try {
			dis.close();
			dos.close();
			socket.close();

			JOptionPane.showMessageDialog(null, "서버와의 연결이 끊겼습니다.", "연결 끊김", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "스트림 close 오류", "오류", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
