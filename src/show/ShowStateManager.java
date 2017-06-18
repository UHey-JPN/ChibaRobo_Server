package show;

import java.util.Calendar;
import java.util.concurrent.Executor;

import data.main.Database;
import keepalive.KeepAliveManager;
import udpSocket.UdpSocket;

public class ShowStateManager implements Runnable {
	private final String CRLF = "\r\n";
	private final int WAIT_TIMES = 5; // 回数
	private ShowState state;
	private PacketID packet_id;
	private ScoreManager score;
	
	private Executor ex;
	private KeepAliveManager kam;
	private Database database;
	
	private UdpSocket udp;
	
	private String str_packet;
	

	public ShowStateManager(Executor ex, KeepAliveManager kam, Database database, UdpSocket udp) {
		this.udp = udp;
		this.state = new ShowState();
		this.packet_id = new PacketID();
		this.score = new ScoreManager();
		this.ex = ex;
		this.kam = kam;
		this.database = database;
		
		set_show("home");
	}
	
	public void set_show(String mode){
		state.set_show(mode);
		set_id();
		ex.execute(this);
	}
	
	public void set_score(int id, int point){
		score.set_score(id, point);
		set_id();
		ex.execute(this);		
	}
	
	public void set_all_score(int p0, int p1){
		score.set_all_score(p0, p1);
		set_id();
		ex.execute(this);		
	}
	
	public void reset_score(){
		score.reset();
		set_id();
		ex.execute(this);		
	}
	
	public void update_teams_robots(){
		set_id();
		ex.execute(this);		
	}
	
	private void set_id(){
		packet_id.new_id();
	}
	
	public int[] get_score(){
		return score.get_point();
	}
	
	public String get_mode(){
		return state.get_mode();
	}
	
	public String get_start_time(){
		Calendar s = state.get_start_time();
		String ret = 
				""  + s.get(Calendar.YEAR) +
				"/" + s.get(Calendar.MONTH) +
				"/" + s.get(Calendar.DATE) +
				" - " + s.get(Calendar.HOUR_OF_DAY) +
				":" + s.get(Calendar.MINUTE) +
				"'" + s.get(Calendar.SECOND) +
				"\""+ s.get(Calendar.MILLISECOND);
		return ret;
	}
	
	public String get_packet(){
		return str_packet;
	}
	
	private void set_packet(){
		// create packet data
		str_packet = "";
		str_packet += "" + packet_id.get_id() + CRLF;
		str_packet += database.get_current_game_no_calc().get_game_id() + CRLF;
		str_packet += "" + state.get_packet();
		str_packet += "" + score.get_packet();
		str_packet += "" + database.get_vs_teams();
		str_packet += "" + database.get_last_winner() + CRLF;
	}
	
	@Override
	public synchronized void run() {
		set_packet();
		
		for(int i = 0; i < 5; i++){
			udp.send_show(str_packet);
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		int counter = WAIT_TIMES;
		while( !kam.isAllChanged(packet_id.get_id()) && counter > 0 ){
			udp.send_show(str_packet);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter--;
		}
		
		System.out.println("all remote host is changed to " + state.get_mode() + ".");
	}

}
