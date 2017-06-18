package keepalive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;


public class KeepAliveManager implements Runnable{
	private List<RemoteHost> list;
	private DatagramSocket soc;
	private int keep_alive_time = 500;

	public KeepAliveManager(Executor ex) {
		list = Collections.synchronizedList(new ArrayList<RemoteHost>());
		while(soc == null){
			try {
				// ソケットを生成。ポート指定はなし。
				soc = new DatagramSocket();
				System.out.println("Keep Alive Socket is opened(port number = " + get_local_port() + ").");
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
		// スレッドの起動
		ex.execute(this);
	}

	public int get_local_port(){
		return soc.getLocalPort();
	}

	@Override
	public void run() {
		while(true){
			try {
				byte[] buf = new byte[256];
				DatagramPacket data = new DatagramPacket(buf, buf.length);
				soc.receive(data);
				String str_pac = new String(buf);
				try {
					getRemoteHost( data.getAddress(), data.getPort() ).receive_packet(str_pac);
				} catch (NoRemoteException e) {
					InetSocketAddress sock_addr
							= new InetSocketAddress(data.getAddress(), data.getPort());
					try{
						list.add( new RemoteHost(str_pac, sock_addr, keep_alive_time) );
					}catch(Exception e1){
						System.out.print("Keep Alive Packet is illegal");
						System.out.print("(from " + data.getAddress() + "/" + data.getPort() + ").");
						System.out.println(str_pac);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	public int get_num_keep_alive(){
		int sum = 0;
		for(RemoteHost h : list){
			if( h.isAlive() ){
				sum++;
			}
		}
		return sum;
	}
	
	public String get_all_state(){
		String ret = "";
		for(RemoteHost h : list){
			ret += h.get_state();
		}
		return ret;		
	}
	
	public boolean isAllChanged(int current_id){
		for(RemoteHost h : list){
			if( h.isAlive() && h.get_current_id() != current_id ){
				return false;
			}
		}
		return true;
	}
	
	private RemoteHost getRemoteHost(InetAddress addr, int port) throws NoRemoteException{
		InetSocketAddress addr_port = new InetSocketAddress(addr, port);
		for(RemoteHost h : list){
			if( h.isSameHost(addr_port) ){
				return h;
			}
		}
		throw new NoRemoteException("" + addr + "/" + port);
	}
	
	private class NoRemoteException extends Exception{
		private static final long serialVersionUID = 1L;
			public NoRemoteException(String message) {
				super("There is not such a remote host.:" + message);
			}
	}
}