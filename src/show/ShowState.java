package show;

import java.util.Calendar;

public class ShowState {
	final static String CRLF = "\r\n";
	private String mode = "";
	private Calendar start_time = Calendar.getInstance();
	private String start_str = "";

	public ShowState(String mode) {
		this.set_show(mode);
	}
	
	public ShowState(){
		this("home");
	}
	
	void set_show(String mode){
		this.mode = mode;
		this.start_time.setTimeInMillis( Calendar.getInstance().getTimeInMillis() + 500 );
		this.start_str =
				"" + start_time.get(Calendar.YEAR) +
				","+ start_time.get(Calendar.MONTH) +
				","+ start_time.get(Calendar.DATE) +
				","+ start_time.get(Calendar.HOUR_OF_DAY) +
				","+ start_time.get(Calendar.MINUTE) +
				","+ start_time.get(Calendar.SECOND) +
				","+ start_time.get(Calendar.MILLISECOND);
	}

	String get_packet(){
		return "" + mode + CRLF + this.start_str + CRLF;
	}
	
	Calendar get_start_time(){
		return start_time;
	}

	public String get_mode() {
		return new String(mode);
	}

}
