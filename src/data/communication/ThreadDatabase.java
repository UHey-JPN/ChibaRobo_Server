package data.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import data.main.Database;

class ThreadDatabase implements Runnable {
	private Socket soc;
	private Database database;

	public ThreadDatabase(Socket soc, Database database) {
		this.soc = soc;
		this.database = database;
	}

	@Override
	public void run() {
		BufferedReader in;
		PrintWriter out;
        String line;
        String[] cmd;
        
		try {
			in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			out = new PrintWriter(soc.getOutputStream(), true);

			line = in.readLine();

			if( line == null ) return;
			cmd = line.split(",");

			if( cmd[0].equals("robot") ){
				// get robot list ----------------------
				out.println("ACK");
				if(cmd.length == 2){
					if( cmd[1].equals("utf-8") ){
						out.println(database.get_xml_robolist("utf-8"));
						System.out.println("Requested robot data(utf-8)");
					}else if( cmd[1].equals("Shift_JIS") ){
						String utf_str = database.get_xml_robolist("Shift_JIS");
						String s_jis = new String(utf_str.getBytes("Shift_JIS"), "Shift_JIS");
						out.println(s_jis);					
						System.out.println("Requested robot data(shift-jis)");
					}else{
						out.println(database.get_xml_robolist());
						System.out.println("Requested robot data(utf-8)");
					}
				}else{
					out.println(database.get_xml_robolist());
					System.out.println("Requested robot data(utf-8)");
				}
			}else if( cmd[0].equals("team") ){
				// get team list ----------------------
				out.println("ACK");
				if(cmd.length == 2){
					if( cmd[1].equals("utf-8") ){
						out.println(database.get_xml_teamlist("utf-8"));
						System.out.println("Requested team data(utf-8)");
					}else if( cmd[1].equals("Shift_JIS") ){
						String utf_str = database.get_xml_teamlist("Shift_JIS");
						String s_jis = new String(utf_str.getBytes("Shift_JIS"), "Shift_JIS");
						out.println(s_jis);					
						System.out.println("Requested team data(shift-jis)");
					}else{
						out.println(database.get_xml_teamlist());
						System.out.println("Requested team data(utf-8)");
					}
				}else{
					out.println(database.get_xml_teamlist());
					System.out.println("Requested team data(utf-8)");
				}
			}else if( cmd[0].equals("tournament") ){
				// get tournament ----------------------
				out.println("ACK");
				if(cmd.length == 2){
					if( cmd[1].equals("utf-8") ){
						out.println(database.get_xml_tournament("utf-8"));
						System.out.println("Requested tournament data(utf-8)");
					}else if( cmd[1].equals("Shift_JIS") ){
						String utf_str = database.get_xml_tournament("Shift_JIS");
						String s_jis = new String(utf_str.getBytes("Shift_JIS"), "Shift_JIS");
						out.println(s_jis);					
						System.out.println("Requested tournament data(shift-jis)");
					}else{
						out.println(database.get_xml_tournament());
						System.out.println("Requested tournament data(utf-8)");
					}
				}else{
					out.println(database.get_xml_tournament());
					System.out.println("Requested tournament data(utf-8)");
				}
			}else{
				// cancel the operation
				out.println("NAK");
				System.out.println("Requested data is failed");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				soc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
	}

}
