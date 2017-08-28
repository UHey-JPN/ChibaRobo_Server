package main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import data.communication.DatabaseTCP;
import data.communication.DatabaseUdp;
import data.main.Database;
import keepalive.KeepAliveManager;
import publicity.Publicity;
import show.ShowStateManager;
import udpSocket.UdpSocket;
import window.main.WindowMain;
import console.ConsoleSocket;

public class ChibaRoboServer {

	public ChibaRoboServer() {
		Executor ex = Executors.newCachedThreadPool();
		try {
			InetAddress addr = InetAddress.getLocalHost();
			System.out.println(addr.getHostName() + "/" + addr.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		UdpSocket udp = new UdpSocket();

		Database database = new Database(24);
		KeepAliveManager kam = new KeepAliveManager(ex);
		ShowStateManager ssm = new ShowStateManager(ex, kam, database, udp);
		DatabaseUdp database_udp = new DatabaseUdp(udp, database, ssm);
		DatabaseTCP database_tcp = new DatabaseTCP(ex, database);
		ConsoleSocket console = new ConsoleSocket(ex, database, ssm, kam);
		Publicity publicity = new Publicity(ex, udp, console.get_local_port(), database_tcp.get_local_port(), kam.get_local_port());
		
		try{
			new WindowMain(ex, udp, database, kam, ssm, database_udp, database_tcp, console, publicity);
		} catch (java.awt.HeadlessException e){
			System.out.println("no window.main mode");
		}
		
		System.out.println("Server System is OK");

		
	}

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		new ChibaRoboServer();
		
	}

}
