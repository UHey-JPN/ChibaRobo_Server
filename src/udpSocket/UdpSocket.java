package udpSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpSocket {
	private final static String CRLF = "\r\n";
	private final static int SOC_PORT = 58239;
	private DatagramSocket soc;
	private InetAddress addr;

	public UdpSocket() {
		try {
			soc = new DatagramSocket();
			System.out.println("UDP Socket is opened(port number = " + soc.getLocalPort() + ").");
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			this.addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public UdpSocket(InetAddress addr, byte[] nic_mac) {
		
		
		try {
			soc = new DatagramSocket(58238, addr);
			System.out.println("UDP Socket is opened(port number = " + soc.getLocalPort() + ").");
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.addr = addr;
	}

	public InetAddress get_inet_address() {
		return this.addr;
	}

	public int get_port() {
		return soc.getLocalPort();
	}

	public synchronized void send_show(String packet) {
		send("show" + CRLF + packet);
	}

	public synchronized void send_tournament(String packet) {
		send("tournament" + CRLF + packet);
	}

	public synchronized void send_server(String packet) {
		send("server" + CRLF + packet);
	}

	private synchronized void send(String str_packet) {
		// set broadcast address
		InetSocketAddress broadcast = new InetSocketAddress("255.255.255.255", SOC_PORT);

		try {
			// create packet
			byte[] b_packet = str_packet.getBytes("UTF-8");
			DatagramPacket packet = new DatagramPacket(b_packet, b_packet.length, broadcast);

			soc.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
