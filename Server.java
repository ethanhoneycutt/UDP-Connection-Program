import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {
	DatagramSocket socket = null;
	int sequenceNum = 0; //start with an expected sequence number of 0
	int port = 9876; //just in case port isnt specified

	public Server(int p) {
		port = p;
	}

	public void createAndListenSocket() {
		System.out.println("Setting up connection...");
		try {
			socket = new DatagramSocket(port); //create socket
			byte[] incomingData = new byte[1024];
			System.out.println("Waiting for client message...");

			while (true) {
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);
				String message = new String(incomingPacket.getData()); //incoming message from client
				System.out.print("Received message from client: " + message);
				//determine sequence number of client message
				int clientSequenceNum = Integer.parseInt(message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" ")));
				
				if(clientSequenceNum == sequenceNum){ //client sequence number matches expected sequence number
					System.out.println("Sequence numbers match, sending ACK");
				}
				else{
					System.out.println("Sequence numbers do no match, sending duplicate ACK");
					sequenceNum--;
				}

				InetAddress IPAddress = incomingPacket.getAddress();
				int port = incomingPacket.getPort();
				String reply = "ACK " + sequenceNum; //build ACK message
				byte[] data = reply.getBytes();
				DatagramPacket replyPacket =
						new DatagramPacket(data, data.length, IPAddress, port);
				socket.send(replyPacket);
				try {
				    Thread.sleep(1000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}

				sequenceNum++;
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length != 1){
			System.out.println("Command: java Server port");
		}
		else{
			Server server = new Server(Integer.parseInt(args[0])); //specify port
			server.createAndListenSocket();
		}
	}
}
