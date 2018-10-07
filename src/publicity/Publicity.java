package publicity;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executor;

import udpSocket.UdpSocket;

public class Publicity implements Runnable {
	private final static String CRLF = "\r\n";
	private int console_port;
	private int database_port;
	private int kam_port;
	private UdpSocket udp;
	private DatagramSocket soc;

	public Publicity(Executor ex, UdpSocket udp, int console_port, int database_port, int kam_port) {
		while (soc == null) {
			try {
				soc = new DatagramSocket();
				System.out.println("Publicity Socket is opened(port number = " + get_port() + ").");
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		this.udp = udp;
		this.console_port = console_port;
		this.database_port = database_port;
		this.kam_port = kam_port;

		ex.execute(this);
	}

	public int get_port() {
		return soc.getLocalPort();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			;
		}
		String str_packet;

		while (true) {
			// create packet data

			// InetAddress addr = InetAddress.getLocalHost();
			InetAddress addr = udp.get_inet_address();
			if (addr == null) {
				// default ip address
				str_packet = "127.0.0.1" + CRLF;
			} else {
				str_packet = addr.getHostAddress() + CRLF;
			}

			//System.out.println(str_packet);

			str_packet += "console," + console_port + CRLF;
			str_packet += "database_port," + database_port + CRLF;
			str_packet += "kam_port," + kam_port + CRLF;

			udp.send_server(str_packet);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
