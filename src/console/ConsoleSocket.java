package console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import data.exception.AllGameIsEndedException;
import data.exception.DataBrokenException;
import data.image.ImageList;
import data.main.Database;
import keepalive.KeepAliveManager;
import show.ShowStateManager;

public class ConsoleSocket implements Runnable{
	public static final String CRLF = "\r\n";
	private ServerSocket listen = null;
	private Database database = null;
	private ShowStateManager ssm = null;
	private KeepAliveManager kam = null;
	private ImageList img_list = null;
	private static String PASSWORD = "chiba.robot.studio";
	private BufferedReader in = null;
	private PrintWriter out = null;

	public ConsoleSocket(
			Executor ex,
			Database database,
			ShowStateManager ssm,
			KeepAliveManager kam,
			ImageList img_list
	) {
		try {
			listen = new ServerSocket(0, 2);
			// listen = new ServerSocket(55123, 2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Console socket is opened(port number = " + get_local_port() + ").");
		this.database = database;
		this.ssm = ssm;
		this.kam = kam;
		this.img_list = img_list;
		
		// スレッドの起動
		ex.execute(this);
	}
	
	public int get_local_port(){
		return listen.getLocalPort();
	}

	@Override
	public void run() {
		Socket soc = null;
		
		while(true){
			try {
				boolean login = false;
				
				// クライアントからの接続要求を待つ。
				System.out.println("CONSOLE : wait for console connection...");
				soc = listen.accept(); 
				String addr_remote = soc.getInetAddress().getHostAddress() + "(" + soc.getPort() + ")";
				System.out.println("connect from " + addr_remote);
				
				// 入出力ストリームの生成
				in  = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
				OutputStreamWriter o_stream = new OutputStreamWriter(soc.getOutputStream(), "UTF-8");
				out = new PrintWriter(new BufferedWriter(o_stream), true);
				
				// -----------------------------------------------------------
				// 認証 -------------------------------------------------------
				// 法を256としたアフィン暗号
				// password : chiba.robot.studio
				
				// 鍵の生成
				int[] i_key = new int[2];
				i_key[0] = ( (int)(Math.random()*128) ) * 2 + 1 ;
				i_key[1] = (int)( Math.random() * 256 );
				System.out.println("key = " + i_key[0] + "," + i_key[1]);
				// 鍵を送信
				out.printf( "" + i_key[0] + "," + i_key[1] + CRLF);
				
				// パスワード暗号文の生成と確認
				String recv_pass = in.readLine();
				char[] true_pass = new char[PASSWORD.length()];
				for(int i = 0; i < PASSWORD.length(); i++){
					true_pass[i] = (char)( (PASSWORD.charAt(i)*i_key[0] + i_key[1])%256);
				}
				if( recv_pass != null ){
					//if( Arrays.equals(recv_pass.toCharArray(), true_pass) ){
					if( recv_pass.equals(PASSWORD) ){
						System.out.println("login successed.");
						out.printf("Login : ACK" + CRLF);
						login = true;
					}else{
						System.out.println("login failed.");
						out.printf("Login : NAK" + CRLF);
						login = false;
					}
				}else{
					login = false;
				}
				
				// コンソールからの命令を処理
				while(login){
					// コマンドライン命令の受け取り
					String str_cmd = in.readLine();
					if( str_cmd == null ) break;
					String[] cmd = str_cmd.split(" ");
					
					// 構文の解析
					if( cmd[0].equals("set") ){
						// syntax of setter ------------------------------
						if( cmd[1].equals("team_num") ){
							database.reset_tournament(Integer.parseInt(cmd[2]));
							out.printf("set the number of team : " + database.get_num_of_team() + "." + CRLF);
							
						}else if( cmd[1].equals("mode") ){
							ssm.set_show( cmd[2] );
							out.printf("set show mode : " + ssm.get_mode() + CRLF);
							
						}else if( cmd[1].equals("score") ){
							if( cmd[2].equals("-clear") ){
								ssm.reset_score();
							}else if( cmd[2].equals("-side0") ){
								ssm.set_score(0, Integer.parseInt(cmd[3]));
							}else if( cmd[2].equals("-side1") ){
								ssm.set_score(1, Integer.parseInt(cmd[3]));
							}else{
								ssm.set_all_score(Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
							}
							out.printf("set score : " + ssm.get_score()[0] + " - " + ssm.get_score()[1] + CRLF);
							
						}else if( cmd[1].equals("winner") ){
							if( cmd[2].equals("side0") ){
								try {
									database.set_winner(0);
									ssm.update_teams_robots();
									out.printf("set winner : side0" + CRLF);
								} catch (DataBrokenException e) {
									out.printf("err:database broken" + CRLF);
									e.printStackTrace();
								} catch (AllGameIsEndedException e) {
									out.printf("All game was finished." + CRLF);
									e.printStackTrace();
								}
							}else if( cmd[2].equals("side1") ){
								try {
									database.set_winner(1);
									ssm.update_teams_robots();
									out.printf("set winner : side1" + CRLF);
								} catch (DataBrokenException e) {
									out.printf("err:database broken" + CRLF);
									e.printStackTrace();
								} catch (AllGameIsEndedException e) {
									out.printf("All game was finished." + CRLF);
									e.printStackTrace();
								}
							}else{
								out.printf("err:2:winner is err:" + cmd[2] + CRLF);
							}
							
						}else if(cmd[1].equals("team_list")){
							try{
								database.set_team_number_list(cmd[2]);
								out.printf("set team_list " + cmd[2] + CRLF);
							} catch (IllegalArgumentException e){
								out.printf("err:2:does not match the number of team." + CRLF);
							}
						}else{
							out.printf("err:1:there is no such a value:" + cmd[1] + CRLF);
						}
						
					}else if( cmd[0].equals("get") ){
						// syntax of getter ------------------------------
						if( cmd[1].equals("team_num") ){
							out.printf("" + database.get_num_of_team() + CRLF);
						}else if( cmd[1].equals("host_list") ){
							out.printf("" + kam.get_all_state() + CRLF);
						}else{
							out.printf("err:1:there is no such a value:" + cmd[1] + CRLF);
						}
						
					}else if( cmd[0].equals("add") ){
						// syntax of add ------------------------------
						if( cmd[1].equals("robot") ){
							add_robot();
						}else if( cmd[1].equals("team") ){
							add_team();
						}else{
							out.printf("err:1:there is no such a value:" + cmd[1] + CRLF);
						}
						
					}else if( cmd[0].equals("clear") ){
						// syntax of add ------------------------------
						if( cmd[1].equals("robot") ){
							database.clear_robolist();
							out.printf("All robot data is cleared." + CRLF);
						}else if( cmd[1].equals("team") ){
							database.clear_teamlist();
							out.printf("All team data is cleared." + CRLF);
						}else{
							out.println("err:1:there is no such a value:" + cmd[1]);
						}
						
					}else if( cmd[0].equals("image") ){
						// syntax of image ------------------------------
						if( cmd[1].equals("add") ){
							img_list.receive_img(cmd[2], out);
							System.out.println("uploading process is finished(name = " + cmd[2] + ").");
						}else if( cmd[1].equals("list") ){
							img_list.update_list();
							out.println(img_list.get_md5_list());
							System.out.println("return the hash list.");
						}
						
					}else if( cmd[0].equals("exit") ){
						// syntax of exit ------------------------------
						out.printf("finish to operation.Logout." + CRLF);
						System.out.println("Logout.");
						break;
						
					}else{
						out.printf("err:0:there is no such a command:" + cmd[0] + CRLF);
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					soc.close();
					System.out.println("ConsoleSocket is closed.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void add_team() {
		int num = 0;
		try {
			String line;
			while( (line = in.readLine()) != null ){
				String[] str = line.split(",");
				
				if( str[0].matches("EOF") ) break;
				
				try {
					int id = Integer.parseInt(str[0]);
					int team0 = Integer.parseInt(str[2]);
					int team1 = Integer.parseInt(str[3]);
					database.add_teamlist(id, str[1], team0, team1, str[4]);
					num++;
				} catch(NumberFormatException e) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.printf(num + " team(s) is registered." + CRLF);
	}

	private void add_robot() {
		int num = 0;
		try {
			String line;
			while( (line = in.readLine()) != null ){
				String[] str = line.split(",");
				if( str[0].matches("EOF") ) break;
				
				String data = str[1];
				for(int i = 2; i < str.length; i++){
					data += "," + str[i];
				}
				try {
					int id = Integer.parseInt(str[0]);
					database.add_robolist(id, data);
					num++;

				} catch(NumberFormatException e) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.printf(num + " robot(s) is registered." + CRLF);
	}

}
