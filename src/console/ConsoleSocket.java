package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
				in  = new BufferedReader(new InputStreamReader(soc.getInputStream()) );
				out = new PrintWriter(soc.getOutputStream(), true);
				
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
				out.println( "" + i_key[0] + "," + i_key[1]);
				
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
						out.println("Login : ACK");
						login = true;
					}else{
						System.out.println("login failed.");
						out.println("Login : NAK");
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
							out.println("set the number of team : " + database.get_num_of_team() + ".");
							
						}else if( cmd[1].equals("mode") ){
							ssm.set_show( cmd[2] );
							out.println("set show mode : " + ssm.get_mode());
							
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
							out.println("set score : " + ssm.get_score()[0] + " - " + ssm.get_score()[1]);
							
						}else if( cmd[1].equals("winner") ){
							if( cmd[2].equals("side0") ){
								try {
									database.set_winner(0);
									ssm.update_teams_robots();
									out.println("set winner : side0");
								} catch (DataBrokenException e) {
									out.println("err:database broken");
									e.printStackTrace();
								} catch (AllGameIsEndedException e) {
									out.println("All game was finished.");
									e.printStackTrace();
								}
							}else if( cmd[2].equals("side1") ){
								try {
									database.set_winner(1);
									ssm.update_teams_robots();
									out.println("set winner : side1");
								} catch (DataBrokenException e) {
									out.println("err:database broken");
									e.printStackTrace();
								} catch (AllGameIsEndedException e) {
									out.println("All game was finished.");
									e.printStackTrace();
								}
							}else{
								out.println("err:2:winner is err:" + cmd[2]);
							}
							
						}else if(cmd[1].equals("team_list")){
							try{
								database.set_team_number_list(cmd[2]);
								out.println("set team_list " + cmd[2]);
							} catch (IllegalArgumentException e){
								out.println("err:2:does not match the number of team.");
							}
						}else{
							out.println("err:1:there is no such a value:" + cmd[1]);
						}
						
					}else if( cmd[0].equals("get") ){
						// syntax of getter ------------------------------
						if( cmd[1].equals("team_num") ){
							out.println("" + database.get_num_of_team());
						}else if( cmd[1].equals("host_list") ){
							out.println("" + kam.get_all_state());
						}else{
							out.println("err:1:there is no such a value:" + cmd[1]);
						}
						
					}else if( cmd[0].equals("add") ){
						// syntax of add ------------------------------
						if( cmd[1].equals("robot") ){
							add_robot();
						}else if( cmd[1].equals("team") ){
							add_team();
						}else{
							out.println("err:1:there is no such a value:" + cmd[1]);
						}
						
					}else if( cmd[0].equals("clear") ){
						// syntax of add ------------------------------
						if( cmd[1].equals("robot") ){
							database.clear_robolist();
							out.println("All robot data is cleared.");
						}else if( cmd[1].equals("team") ){
							database.clear_teamlist();
							out.println("All team data is cleared.");
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
						out.println("finish to operation.Logout.");
						System.out.println("Logout.");
						break;
						
					}else{
						out.println("err:0:there is no such a command:" + cmd[0]);
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
		out.println(num + " team(s) is registered."); 
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
		out.println(num + " robot(s) is registered."); 
	}

}
