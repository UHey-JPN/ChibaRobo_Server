package keepalive;

import java.net.InetSocketAddress;
import java.util.Calendar;

class RemoteHost {
	private String name;
	InetSocketAddress remote_addr;	
	private int ttka_ms;
	private int current_id;
	private Calendar last_time;

	RemoteHost(String packet, InetSocketAddress remote_addr, int ttka_ms){
		String[] str = packet.split(",");
		this.name = str[1];
		this.remote_addr = remote_addr;
		this.ttka_ms = ttka_ms;
		this.receive_packet(packet);
	}
	
	boolean isSameHost(InetSocketAddress remote_addr){
		return this.remote_addr.equals(remote_addr);
	}
	
	void set_ttks(int time){
		this.ttka_ms = time;
	}
	
	void receive_packet(String packet){
		String[] str = packet.split(",");
		this.current_id = Integer.parseInt(str[0]);
		this.last_time = Calendar.getInstance();
	}
	
	boolean isAlive(){
		int offset = this.last_time.compareTo(Calendar.getInstance());
		return (offset > this.ttka_ms);
	}
	
	String get_state(){
		String alive = (isAlive()) ? "ALIVE" : "DEAD";
		String str = this.name + "," + alive + ","
				+ remote_addr.getHostName() + ","
				+ remote_addr.getPort() + "\n";
		return str;
	}
	
	int get_current_id(){
		return current_id;
	}
}
