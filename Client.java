import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {
	DatagramSocket Socket;
	String host = "";
	int port = 9876;
	Scanner reader = new Scanner(System.in);
	int sequenceNum = 0;
	InetAddress IPAddress;

	public Client(String h, int p) {
		host = h;
		port = p;
	}

	public void createAndListenSocket() {

		System.out.println("Setting up connection...");
		try{
			Socket = new DatagramSocket();
			IPAddress = InetAddress.getByName(host); //determine IP
		} catch(SocketException e){
			e.printStackTrace();
		} catch(UnknownHostException e){
			e.printStackTrace();
		}

		Boolean stop = false;
		while(!stop){
			System.out.print("Enter one character: ");
			char input = read(reader); //take one character from input

			byte[] outgoingData = new byte[1024];
			String sentence = "DATA " + sequenceNum + " " + input + "\n"; //build message to send
			byte[] data = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
			DatagramPacket incomingPacket = new DatagramPacket(outgoingData, outgoingData.length);

			sendPacketAttempt(Socket, sendPacket, incomingPacket); //recursive function handler

			sequenceNum++;
		}

		Socket.close();
		System.out.println("Connection closed.");

	}

	public char read(Scanner reader){
		char c = reader.next().charAt(0);
		return c;
	}

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage: java Client host port");
		}
		else{
			Client client = new Client(args[0], Integer.parseInt(args[1]));
			client.createAndListenSocket();
		}
	}

	public void sendPacketAttempt(DatagramSocket Socket, DatagramPacket sendPacket, DatagramPacket incomingPacket){
		try{
			Socket.send(sendPacket); //send message to server
		} catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Message sent: " + new String(sendPacket.getData()));

		try {
			Socket.setSoTimeout(5000); //set 5 second timeout
		} catch (SocketException e) {
			System.out.println("Error setting timeout" + e);
		}

		try{
			Socket.receive(incomingPacket); //see if server responds
		} catch(SocketTimeoutException e){
			System.out.println("Server response not found, resending...");
			//if server doesn't respond, resend message
			sendPacketAttempt(Socket, sendPacket, incomingPacket);
		} catch(IOException e){
			e.printStackTrace();
		}
		String response = new String(incomingPacket.getData());
		//handle server's response
		System.out.println("Response from server: " + response);
		int serverSequenceNum = Integer.parseInt(response.substring(response.indexOf(" ") + 1, response.indexOf(" ") + 2));
		//find returned sequence number of ACK
		if(serverSequenceNum == Integer.parseInt((new String(sendPacket.getData())).substring((new String(sendPacket.getData())).indexOf(" ") + 1, (new String(sendPacket.getData())).lastIndexOf(" ")))){
			System.out.println("Correct ACK received: " + serverSequenceNum + ", continuing...");
		} else{
			System.out.println("Incorrect ACK received: " + serverSequenceNum + ", waiting...");
			try {
				Socket.setSoTimeout(5000);
			} catch (SocketException e) {
				System.out.println("Error setting timeout" + e);
			}

			String sentence = "DATA " + serverSequenceNum + new String(sendPacket.getData()).substring(new String(sendPacket.getData()).lastIndexOf(" "));
			byte[] data = sentence.getBytes();
			sendPacket = new DatagramPacket(data, data.length, IPAddress, port);

			sendPacketAttempt(Socket, sendPacket, incomingPacket);
			//retry if failure
		}

		return;
	}
}
