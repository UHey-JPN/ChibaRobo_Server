package data.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
			in = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
			OutputStreamWriter o_stream = new OutputStreamWriter(soc.getOutputStream(), "UTF-8");
			out = new PrintWriter(new BufferedWriter(o_stream), true);

			line = in.readLine();

			if( line == null ) return;
			cmd = line.split(",");

			if( cmd[0].equals("robot") ){
				// get robot list ----------------------
				out.printf("ACK" + CRLF);
				if(cmd.length == 2 || cmd.length == 1){
					out.printf(database.get_xml_robolist("utf-8") + CRLF);
					System.out.println("Requested robot data(utf-8)");
				}else{
					out.printf(database.get_xml_robolist() + CRLF);
					System.out.println("Requested robot data(utf-8)");
				}
				
			}else if( cmd[0].equals("team") ){
				// get team list ----------------------
				out.printf("ACK" + CRLF);
				if(cmd.length == 2 || cmd.length == 1){
					out.printf(database.get_xml_teamlist("utf-8") + CRLF);
					System.out.println("Requested team data(utf-8)");
				}else{
					out.printf(database.get_xml_teamlist() + CRLF);
					System.out.println("Requested team data(utf-8)");
				}
				
			}else if( cmd[0].equals("tournament") ){
				// get tournament ----------------------
				out.printf("ACK" + CRLF);
				if(cmd.length == 2 || cmd.length == 1){
					out.printf(database.get_xml_tournament("utf-8") + CRLF);
					System.out.println("Requested tournament data(utf-8)");
				}else{
					out.printf(database.get_xml_tournament() + CRLF);
					System.out.println("Requested tournament data(utf-8)");
				}
				
			}else if( cmd[0].equals("image") ){
				// get image ----------------------
				if(cmd.length != 2){
					out.printf("NAK:ileagal command.".getBytes() + CRLF);
				}else{
					try {
						Image img = img_list.get(cmd[1]);
						String ack = "ACK:" + img.size() + CRLF;
						soc.getOutputStream().write( ack.getBytes() );
						img.upload_data(soc.getOutputStream());
						
					} catch (FileNotFoundException e){
						out.printf("NAK:cannot find such a file(" + cmd[1] + ")." + CRLF);
					}
				}
				
			}else{
				// cancel the operation
				out.printf("NAK" + CRLF);
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
