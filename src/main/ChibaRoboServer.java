package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

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

		InetAddress nic_addr = SettingManager.getIpAddr();
		byte[] nic_mac = SettingManager.getNicMac();
		int db_port = SettingManager.getDbPort();

		LogToSystemIO log = new LogToSystemIO();
		ImageList img_list = new ImageList("DB/img/", log);
		Database database = new Database(24);
		
		DatabaseTCP database_tcp = null;
		try {
			database_tcp = new DatabaseTCP(ex, database, img_list, db_port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		UdpSocket udp = new UdpSocket(nic_addr, nic_mac);
		KeepAliveManager kam = new KeepAliveManager(ex);
		ShowStateManager ssm = new ShowStateManager(ex, kam, database, udp);
		DatabaseUdp database_udp = new DatabaseUdp(udp, database, ssm);
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
