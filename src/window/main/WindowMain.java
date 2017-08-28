package window.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.concurrent.Executor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import console.ConsoleSocket;
import data.communication.DatabaseTCP;
import data.communication.DatabaseUdp;
import data.main.Database;
import keepalive.KeepAliveManager;
import publicity.Publicity;
import show.ShowStateManager;
import udpSocket.UdpSocket;

public class WindowMain extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	private static final int NUM_FUNC = 5;
	private UdpSocket udp;
	private KeepAliveManager kam;
	private ShowStateManager ssm;
	private DatabaseTCP db_tcp;
	private ConsoleSocket console;
	private Publicity publicity;
	
	private String[] func_name = {
			"UDP Socket to send",
			"Keep ALive Manager",
			"TCP Socket to get database",
			"TCP Console",
			"Publicity"
	};
	private JPanel[] func_state = new JPanel[NUM_FUNC];
	private JPanel show_state = new JPanel();
	
	private JLabel str_mode;
	private JLabel str_s_time;


	public WindowMain(
			Executor ex,
			UdpSocket udp,
			Database db,
			KeepAliveManager kam,
			ShowStateManager ssm,
			DatabaseUdp db_udp,
			DatabaseTCP sb_tcp,
			ConsoleSocket console,
			Publicity publicity
		){
		
		// data hold
		this.udp = udp;
		this.kam = kam;
		this.ssm = ssm;
		this.db_tcp = sb_tcp;
		this.console = console;
		this.publicity = publicity;

		this.setTitle("Chiba Robo Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(480, 320);
		
		// create instance
		for(int i = 0; i < NUM_FUNC; i++){
			func_state[i] = new JPanel();
			func_state[i].setBorder( new LineBorder(Color.BLACK, 1) );
		}
		show_state.setBorder( new LineBorder(Color.BLACK, 1) );
		
		// set layout
		JPanel p = new JPanel();
		p.setLayout( new GridLayout(NUM_FUNC+1, 2) );
		
		{// show state
			str_mode = new JLabel( ssm.get_mode() );
			str_s_time = new JLabel( ssm.get_start_time() );
			
			str_mode.setHorizontalAlignment(JLabel.CENTER);
			str_s_time.setHorizontalAlignment(JLabel.CENTER);
			
			JLabel name = new JLabel("Show Status");
			name.setHorizontalAlignment(JLabel.CENTER);
			name.setBorder( new LineBorder(Color.BLACK, 1) );
			
			show_state.setLayout(new GridLayout(2,1));
			
			show_state.add(str_mode);
			show_state.add(str_s_time);
			
			p.add( name );
			p.add(show_state);
		}
		
		update_status();
		update_show();
		
		// set contents
		for(int i = 0; i < NUM_FUNC; i++){
			JLabel name = new JLabel(func_name[i]);
			name.setHorizontalAlignment(JLabel.CENTER);
			name.setBorder( new LineBorder(Color.BLACK, 1) );
			p.add( name );
			p.add( func_state[i] );
		}
		
		getContentPane().add(p, BorderLayout.CENTER);
		
		p.setBackground(Color.WHITE);
		p.setOpaque(true);
		this.setVisible(true);
		
		// thread
		ex.execute(this);
	}
	
	private void update_status(){
		int[] func_port = new int[5];
		
		func_port[0] = udp.get_port();
		func_port[1] = kam.get_local_port();
		func_port[2] = db_tcp.get_local_port();
		func_port[3] = console.get_local_port();
		func_port[4] = publicity.get_port();
		
		for(int i = 0; i < NUM_FUNC; i++){
			JLabel status;
			JLabel port = new JLabel( String.valueOf(func_port[i]) ) ;
			
			if( func_port[i] > 0 ){
				status = new JLabel("OK");
				func_state[i].setBackground(Color.GREEN);
			}else{
				status = new JLabel("NG");
				func_state[i].setBackground(Color.RED);
			}
			func_state[i].setOpaque( true );
			
			func_state[i].setLayout( new GridLayout(2, 1) );
			status.setHorizontalAlignment(JLabel.CENTER);
			port.setHorizontalAlignment(JLabel.CENTER);
			func_state[i].add(status);
			func_state[i].add(port);
		}
	}
	
	private void update_show(){
		str_mode.setText( ssm.get_mode() );
		str_s_time.setText( ssm.get_start_time() );
	}

	@Override
	public void run() {
		while(true){
			update_show();
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
