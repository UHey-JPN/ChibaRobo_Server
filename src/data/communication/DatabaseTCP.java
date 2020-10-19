package data.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import javax.swing.JOptionPane;

import data.image.ImageList;
import data.main.Database;

public class DatabaseTCP implements Runnable {
	private Executor ex;
	private Database database;
	private ImageList img_list;
	private ServerSocket listen;

	public DatabaseTCP(Executor ex, Database database, ImageList img_list, int db_port) throws IOException {
		this.ex = ex;
		this.database = database;
		this.img_list = img_list;

		try {
			listen = new ServerSocket(db_port, 2);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to bind to TCP port (is another process using it?).");
		}

		System.out.println("Database socket is opened(port number = " + this.get_local_port() + ").");

		ex.execute(this);
	}

	public int get_local_port() {
		return listen.getLocalPort();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket soc = listen.accept();
				ex.execute(new ThreadDatabase(soc, database, img_list));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
