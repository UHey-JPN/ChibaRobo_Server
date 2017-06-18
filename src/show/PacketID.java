package show;

class PacketID {
	private final int QUEUE_SIZE = 10;
	private int id;
	private int cnt;
	private int[] old_list;

	PacketID() {
		id = 1;
		cnt = 0;
		old_list = new int[QUEUE_SIZE];
		old_list[cnt] = 0;
		cnt++;
	}
	
	int new_id(){
		while(true){
			int new_id = (int)( Math.random() * 65535 );
			for(int i = 0; i < old_list.length; i++){
				if(old_list[i] == new_id) continue;
			}
			id = new_id;
			break;
		}
		
		// add the new number to queue.
		old_list[cnt] = id;
		
		// management of queue counter
		cnt++;
		if(cnt >= this.QUEUE_SIZE) cnt = 0;
		
		return id;
	}
	
	int get_id() { return id; }

}
