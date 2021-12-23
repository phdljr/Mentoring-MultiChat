package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;


public class Server {
	private int port;
	
	private ServerSocket serverSocket;
	
	// 클라이언트와 연결된 소캣을 저장는 벡터
	private Vector<ClientInfo> clientVector;
	
	public Server(int port) {
		this.port = port;
		this.clientVector = new Vector<ClientInfo>();
		
		if(openServer()) {
			acceptClient();
		}
	}
	
	private boolean openServer() {
		System.out.println("포트번호 " + port + " 소켓 생성");
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("오류 : 이미 사용중인 포트 번호");
			return false;
		}

		return (serverSocket != null);
	}
	
	private void acceptClient() {
		new Thread(() -> {
			while (true) {
				try {
					System.out.println("클라이언트의 접속 요청을 기다리는 중...");
					Socket socket = serverSocket.accept();

					ClientInfo ci = new ClientInfo(socket);
					clientVector.add(ci);

					System.out.println("클라이언트" + socket.getInetAddress().toString() + "의 접속 요청 수신");
				} catch (IOException e) { // 오류 일어나면 서버 종료
					// e.printStackTrace();
					closeServer();
				}
			}
		}).start();
	}
	
	private void closeServer() {
		try {
			serverSocket.close();
			System.out.println("서버 종료");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ClientInfo{
		private Socket socket;
		
		private InputStream is;
		private DataInputStream dis;
		private OutputStream os;
		private DataOutputStream dos;
		
		public ClientInfo(Socket socket) {
			this.socket = socket;
			
			connect();
		}
		
		private void connect() {
			try {
				is = socket.getInputStream();
				dis = new DataInputStream(is);
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
			runThreadToGetMessage();
		}
		
		private void runThreadToGetMessage() {
			new Thread(() -> {
				while (true) {
					try {
						String msg = dis.readUTF(); // 클라이언트로부터 메세지를 받아들이는 부분
						inMessageFromClient(msg); // 받은 메세지를 분석하는 메소드
					} catch (IOException e) { // 클라이언트로 연결이 끊길 시
						try {
							clientVector.remove(this);
							dis.close();
							dos.close();
							socket.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						System.out.println("클라이언트 연결 끊김");
						break;
					}
				}
			}).start();
		}
		
		public void inMessageFromClient(String msg) {
			System.out.println("클라이언트로부터 온 메세지 : " + msg);

			StringTokenizer st = new StringTokenizer(msg, "/");

			String protocol = st.nextToken();
			String data = st.nextToken();
			
			System.out.println("프로토콜 : "+protocol);
			System.out.println("내용 : "+data);
			
			if(protocol.equals("Hello")) {
				sendMessageToClient("Hello/안녕, 클라이언트!");
			}
			else if(protocol.equals("Chat")) {
				broadcast(msg);
			}
		}
		
		public void sendMessageToClient(String msg) {
			try {
				dos.writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void broadcast(String msg) {
			for(ClientInfo c : clientVector) {
				c.sendMessageToClient(msg);
			}
		}
	}
}
