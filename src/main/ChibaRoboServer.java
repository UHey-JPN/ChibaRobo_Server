package main;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
		System.out.println("Applying config...");

		try {
			SettingManager.ReadConfiguration(ChibaRoboServer.class);
		} catch (IOException e1) {
			System.out.println(
					"An IOException was thrown while trying to open/read from configuration file. (does the file exist or someone else is using it?)");
			e1.printStackTrace();
		}

		InetAddress addr = null;
		InetAddress nic_addr = null;
		byte[] nic_mac = null;
		String nic_addr_str = SettingManager.getProperties().getProperty("IP_ADDR");
		String nic_mac_str = SettingManager.getProperties().getProperty("NIC_MAC");

		if (nic_addr_str != null) {
			try {
				nic_addr = InetAddress.getByName(nic_addr_str);
			} catch (UnknownHostException e) {
				// e.printStackTrace();
				nic_addr = null;
			}
		}

		if (nic_mac_str != null) {
			try {
				long nic_mac_long = Long.parseLong(nic_mac_str.replaceAll(":", ""), 16);
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
				buffer.putLong(nic_mac_long);
				nic_mac = Arrays.copyOfRange(buffer.array(), 2, 8);
			} catch (Exception e) {
				// e.printStackTrace();
				nic_mac = null;
			}
		}

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

				// System.out.println("Using NIC: " + e.getName() + " : " + e.getDisplayName());

				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress _addr = a.nextElement();
					
					if(_addr.equals(nic_addr))
					{
						System.out.println("Found NIC matching IP address: " + e.getName() + " : " + e.getDisplayName());
						addr = _addr;
						break;
					}
					
					if(addr == null && mac_matches && _addr instanceof Inet4Address)
					{
						System.out.println("Found NIC matching MAC address: " + e.getName() + " : " + e.getDisplayName());
						addr = _addr;
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("An error occurred while trying to get local IP addresses.");
			e.printStackTrace();
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

		UdpSocket udp = new UdpSocket(addr, nic_mac);

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

		System.out.println("Server System looks OK");

	}

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		new ChibaRoboServer();

	}

}
