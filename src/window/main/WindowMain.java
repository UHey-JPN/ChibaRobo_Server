package window.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.concurrent.Executor;

import javax.swing.BoxLayout;
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
	private static final int ROW_NUM = NUM_FUNC + 3;
	private UdpSocket udp;
	private KeepAliveManager kam;
	private ShowStateManager ssm;
	private DatabaseTCP db_tcp;
	private ConsoleSocket console;
	private Publicity publicity;

	private String[] func_name = { "Master (UDP)", "Keep-alive", "Database (TCP)", "Console (TCP)", "Publicity" };
	private JPanel[] func_state = new JPanel[NUM_FUNC];
	private JPanel nic_state = new JPanel();
	private JPanel show_state = new JPanel();

	private JLabel label_mode;
	private JLabel label_s_time;

	public WindowMain(Executor ex, UdpSocket udp, Database db, KeepAliveManager kam, ShowStateManager ssm,
			DatabaseUdp db_udp, DatabaseTCP sb_tcp, ConsoleSocket console, Publicity publicity) {

		// data hold
		this.udp = udp;
		this.kam = kam;
		this.ssm = ssm;
		this.db_tcp = sb_tcp;
		this.console = console;
		this.publicity = publicity;

		this.setTitle("ChibaRobo Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(480, 320);
		this.setPreferredSize(new Dimension(200, 20));

		// create instance
		for (int i = 0; i < NUM_FUNC; i++) {
			func_state[i] = new JPanel();
			func_state[i].setBorder(new LineBorder(Color.BLACK, 1));
		}
		show_state.setBorder(new LineBorder(Color.BLACK, 1));

		// set layout
		JPanel p = new JPanel();
		// p.setLayout( new GridLayout(ROW_NUM, 2) );
		p.setLayout(new BorderLayout());

		var panel_left = new JPanel();
		var panel_right = new JPanel();
		panel_left.setLayout(new GridLayout(ROW_NUM, 1));
		panel_right.setLayout(new GridLayout(ROW_NUM, 1));
		panel_left.setPreferredSize(new Dimension(120, 20));
		panel_right.setPreferredSize(new Dimension(200, 20));
		getContentPane().add(panel_left, BorderLayout.WEST);
		getContentPane().add(panel_right, BorderLayout.CENTER);
		//getContentPane().add(p, BorderLayout.CENTER);

		{// show state
			label_mode = new JLabel(ssm.get_mode());
			label_s_time = new JLabel(ssm.get_start_time());

			label_mode.setHorizontalAlignment(JLabel.CENTER);
			label_s_time.setHorizontalAlignment(JLabel.CENTER);

			JLabel name = new JLabel("Show Status");
			name.setHorizontalAlignment(JLabel.CENTER);
			name.setBorder(new LineBorder(Color.BLACK, 1));
			//label_mode.setBorder(new LineBorder(Color.BLACK, 1));

			show_state.setLayout(new GridLayout(2, 1));

			show_state.add(label_mode);
			show_state.add(label_s_time);

			panel_left.add(name);
			panel_right.add(show_state);
		}

		JLabel nic_name = new JLabel("Current NIC");
		nic_name.setHorizontalAlignment(JLabel.CENTER);
		nic_name.setBorder(new LineBorder(Color.BLACK, 1));
		panel_left.add(nic_name);

		String nic_text = udp.getNic().getName() + " (" + udp.getNic().getDisplayName() + ")";
		JLabel nic_status = new JLabel(nic_text);
		nic_status.setToolTipText(nic_text);
		nic_status.setHorizontalAlignment(JLabel.CENTER);
		nic_status.setBorder(new LineBorder(Color.BLACK, 1));
		panel_right.add(nic_status);

		JLabel label_ip_hdr = new JLabel("IP Addr.");
		label_ip_hdr.setHorizontalAlignment(JLabel.CENTER);
		label_ip_hdr.setBorder(new LineBorder(Color.BLACK, 1));
		panel_left.add(label_ip_hdr);
		
		JLabel label_ip = new JLabel(udp.get_inet_address().getHostAddress());
		label_ip.setHorizontalAlignment(JLabel.CENTER);
		label_ip.setBorder(new LineBorder(Color.BLACK, 1));
		panel_right.add(label_ip);

		update_status();
		update_show();

		// set contents
		for (int i = 0; i < NUM_FUNC; i++) {
			JLabel name = new JLabel(func_name[i]);
			name.setHorizontalAlignment(JLabel.CENTER);
			name.setBorder(new LineBorder(Color.BLACK, 1));
			panel_left.add(name);
			panel_right.add(func_state[i]);
		}

		panel_left.setBackground(Color.WHITE);
		panel_left.setOpaque(true);
		//panel_right.setBackground(Color.WHITE);
		//panel_right.setOpaque(true);
		this.pack();
		this.setMinimumSize(new Dimension(320, 279));
		this.setVisible(true);

		// thread
		ex.execute(this);
	}

	private void update_status() {
		int[] func_port = new int[5];

		func_port[0] = udp.get_port();
		func_port[1] = kam.get_local_port();
		func_port[2] = db_tcp.get_local_port();
		func_port[3] = console.get_local_port();
		func_port[4] = publicity.get_port();

		for (int i = 0; i < NUM_FUNC; i++) {
			// JLabel port = new JLabel( String.valueOf(func_port[i]) ) ;
			String port_text = String.valueOf(func_port[i]);

			if (func_port[i] > 0) {
				// status = new JLabel("OK");
				port_text += " (OK) ";
				func_state[i].setBackground(Color.GREEN);
			} else {
				// status = new JLabel("NG");
				port_text += " (NG) ";
				func_state[i].setBackground(Color.RED);
			}
			func_state[i].setOpaque(true);

			func_state[i].setLayout(new GridLayout(1, 1));
			// status.setHorizontalAlignment(JLabel.LEFT);
			JLabel port_status = new JLabel(port_text);
			port_status.setHorizontalAlignment(JLabel.CENTER);
			func_state[i].add(port_status);
			// func_state[i].add(status);
		}
	}

	private void update_show() {
		label_mode.setText(ssm.get_mode());
		label_s_time.setText(ssm.get_start_time());
	}

	@Override
	public void run() {
		while (true) {
			update_show();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
