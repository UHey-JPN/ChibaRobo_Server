package data.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import data.main.Database;

public class DatabaseTCP implements Runnable {
	private Executor ex;
	private Database database;
	private ServerSocket listen;
	
	public DatabaseTCP(Executor ex, Database database) {
		this.ex = ex;
		this.database = database;
		
		try {
			listen = new ServerSocket(0, 2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Database socket is opened(port number = " + get_local_port() + ").");

		ex.execute(this);
		
	}

	public int get_local_port() {
		return listen.getLocalPort();
	}

	@Override
	public void run() {
		while(true){
			try {
				Socket soc = listen.accept();
				ex.execute( new ThreadDatabase(soc, database) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
