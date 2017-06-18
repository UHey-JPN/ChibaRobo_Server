package data.communication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import data.main.Database;
import show.ShowStateManager;
import udpSocket.UdpSocket;

public class DatabaseUdp implements Runnable {
	private final static String CRLF = "\r\n";
	private UdpSocket udp;
	private Database database;
	private ShowStateManager ssm;

	public DatabaseUdp(UdpSocket udp, Database database, ShowStateManager ssm) {
		this.udp = udp;
		this.database = database;
		this.ssm = ssm;
		
		ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
		ex.scheduleAtFixedRate(this, 10, 1000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		udp.send_tournament(database.get_tournament_result() + CRLF);
		udp.send_show(ssm.get_packet());
	}

}
