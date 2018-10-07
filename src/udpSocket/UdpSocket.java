package udpSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;


public class UdpSocket {
	private final static String CRLF = "\r\n";
	private final static int SOC_PORT = 58239;
	private DatagramSocket soc;
	private InetAddress addr;

	public UdpSocket(InetAddress nic_addr, byte[] nic_mac) {
		InetAddress addr = null;
		try {
			// enumerate all NIC
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while ((addr == null || addr.isLoopbackAddress()) && n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();

				byte[] mac = e.getHardwareAddress();
				boolean mac_matches = false;
				if (mac != null && Arrays.equals(mac, nic_mac)) {
					mac_matches = true;
				}

				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress _addr = a.nextElement();

					if (_addr.equals(nic_addr)) {
						System.out.println(
								"Found an NIC with matching IP address: " + e.getName() + " : " + e.getDisplayName());
						addr = _addr;
						break;
					}

					if (addr == null && mac_matches && _addr instanceof Inet4Address) {
						System.out.println(
								"Found an NIC with matching MAC address: " + e.getName() + " : " + e.getDisplayName());
						addr = _addr;
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("An error occurred while trying to get local IP address.");
			e.printStackTrace();
			addr = null;
		}

		if (addr == null) {
			try {
				addr = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				System.out.println("An UnknownHostException was thrown while trying to get local IP addresses.");
				e.printStackTrace();
			}
		}

		System.out.println(addr.getHostName() + "/" + addr.getHostAddress());

		try {
			soc = new DatagramSocket(null);
			soc.setBroadcast(true);
			soc.bind(new InetSocketAddress(addr, 0));
			System.out.println("Bound to UDP port " + soc.getLocalPort() + ".");
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
