package main;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import data.communication.DatabaseTCP;
import data.communication.DatabaseUdp;
import data.image.ImageList;
import data.main.Database;
import keepalive.KeepAliveManager;
import publicity.Publicity;
import show.ShowStateManager;
import udpSocket.UdpSocket;
import window.logger.LogToSystemIO;
import window.main.WindowMain;
import console.ConsoleSocket;

public class ChibaRoboServer {

	public ChibaRoboServer() {
		Executor ex = Executors.newCachedThreadPool();

		System.out.println("Starting ChibaRobo Server...");

		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("An UnknownHostException was thrown while trying to get local IP addresses.");
			e.printStackTrace();
		}

		// enumerate all NIC
		if (addr == null || addr.isLoopbackAddress()) {
			try {
				Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
				while ((addr == null || addr.isLoopbackAddress()) && n.hasMoreElements()) {
					NetworkInterface e = n.nextElement();
					System.out.println("Network interface detected: " + e.getName());
					Enumeration<InetAddress> a = e.getInetAddresses();
					while (a.hasMoreElements()) {
						InetAddress _addr = a.nextElement();
						System.out.println("  " + _addr.getHostAddress());
						if(!_addr.isLoopbackAddress() && (_addr instanceof Inet4Address)) {
							addr = _addr;
							break;
						}
					}
				}
			} catch (SocketException e) {
				System.out.println("An error occurred while trying to get local IP addresses.");
				e.printStackTrace();
			}
		}

		System.out.println(addr.getHostName() + "/" + addr.getHostAddress());

		UdpSocket udp = new UdpSocket(addr);

		LogToSystemIO log = new LogToSystemIO();

		ImageList img_list = new ImageList("DB/img/", log);
		Database database = new Database(24);
		KeepAliveManager kam = new KeepAliveManager(ex);
		ShowStateManager ssm = new ShowStateManager(ex, kam, database, udp);
		DatabaseUdp database_udp = new DatabaseUdp(udp, database, ssm);
		DatabaseTCP database_tcp = new DatabaseTCP(ex, database, img_list);
		ConsoleSocket console = new ConsoleSocket(ex, database, ssm, kam, img_list);
		Publicity publicity = new Publicity(ex, udp, console.get_local_port(), database_tcp.get_local_port(),
				kam.get_local_port());

		try {
			new WindowMain(ex, udp, database, kam, ssm, database_udp, database_tcp, console, publicity);
		} catch (java.awt.HeadlessException e) {
			System.out.println("no window.main mode");
		}

		System.out.println("Server System is OK");

	}

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		new ChibaRoboServer();

	}

}
