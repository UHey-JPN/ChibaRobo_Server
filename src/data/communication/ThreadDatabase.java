package data.communication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import data.image.Image;
import data.image.ImageList;
import data.main.Database;

class ThreadDatabase implements Runnable {
	public static final String CRLF = "\r\n";
	
	private Socket soc;
	private Database database;
	private ImageList img_list;

	public ThreadDatabase(Socket soc, Database database, ImageList img_list) {
		this.soc = soc;
		this.database = database;
		this.img_list = img_list;
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
				
			}else if( cmd[0].equals("image") ){
				// get image ----------------------
				if(cmd.length != 2){
					out.println("NAK:ileagal command.".getBytes());
				}else{
					try {
						Image img = img_list.get(cmd[1]);
						String ack = "ACK:" + img.size() + CRLF;
						soc.getOutputStream().write( ack.getBytes() );
						img.upload_data(soc.getOutputStream());
						
					} catch (FileNotFoundException e){
						out.println("NAK:cannot find such a file(" + cmd[1] + ").");
					}
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
