package data.tournament;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import data.exception.AllGameIsEndedException;
import data.exception.DataBrokenException;
import data.exception.GameNotEndedException;
import data.exception.IllegalTypeOfClassException;
import data.main.CodeXML;

class Game {
	GameNode root;
	int num_round;
	int[] priority;
	int num_team;
	
	Game(int num) {
		this.num_team = num;
		create_tree();
	}

	// 木構造を生成する
	synchronized void create_tree(){
		calc_priority();
		root = set_game(0, 0);
	}
	synchronized private GameNode set_game(int nest, int in_priority){
		if(nest == num_round){
			return new GameNode(in_priority);
		}else if(nest == num_round-1){
			if( priority[in_priority] == 0 ){
				GameNode ret = new GameNode();
				ret.set_game( 0, set_game(nest+1, (in_priority<<1)+0) );
				ret.set_game( 1, set_game(nest+1, (in_priority<<1)+1) );
				ret.get_game( 0 ).set_number((in_priority<<1)+0);
				ret.get_game( 1 ).set_number((in_priority<<1)+1);
				return ret;
			}else{
				return new GameNode(in_priority);
			}
		}else{
			GameNode ret = new GameNode();
			ret.set_game( 0, set_game(nest+1, (in_priority<<1)+0) );
			ret.set_game( 1, set_game(nest+1, (in_priority<<1)+1) );
			ret.get_game(0).set_number((in_priority<<1)+0);
			ret.get_game(1).set_number((in_priority<<1)+1);
			return ret;
		}
	}

	// 試合番号の計算を行うメソッド。
	synchronized void calc_game_number() {
		int now_num = num_team - 2;
		Queue<GameNode> q = new ArrayDeque<GameNode>();
		q.add(root);
		while(!q.isEmpty()){
			GameNode g = q.remove();
			if(g.isGame()){
				// キューに子ノードを追加
				q.add(g.get_game(1));
				q.add(g.get_game(0));
				// 試合番号の設定
				g.set_number(now_num--);
			}
		}	
	}

	// トーナメント表内のチーム番号を設定する。
	synchronized void set_team(int[] team_list){
		Deque<GameNode> s = new ArrayDeque<GameNode>();
		int number = 0;
		s.addFirst(root);
		while(!s.isEmpty()){
			GameNode g = s.removeFirst();
			if(g.isGame()){
				s.addFirst(g.get_game(1));
				s.addFirst(g.get_game(0));
			}else{
				g.set_number(team_list[number++]);
			}
		}
	}
	
	// 引数で受け取ったノードの勝者を指定する。
	// synchronized を利用するためここで指定
	synchronized public void set_winner(GameNode game, int winner){
		game.set_winner(winner);
	}
	
	// まだ行われていない最新の試合をGameNodeクラスで返す。
	// このメソッドで返ってくるインスタンスは木構造の中にあるインスタンスなので扱いには注意
	synchronized private GameNode get_current_gameNode() throws DataBrokenException, AllGameIsEndedException, IllegalTypeOfClassException{
		GameNode pre_node = root;
		Queue<GameNode> q = new ArrayDeque<GameNode>();
		if( root.isEnded() ){
			try {
				throw new AllGameIsEndedException(root.get_game(root.get_winner_int()).get_winner().get_team());
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (GameNotEndedException e) {
				e.printStackTrace();
			}
		}
		q.add(root);
		while(!q.isEmpty()){
			GameNode g = q.remove();
			if(g.isGame()){
				// キューに子ノードを追加
				q.add(g.get_game(1));
				q.add(g.get_game(0));
				// 最新の試合かを確認
				try {
					if(g.isEnded() == true) return pre_node;
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if(g.get_game_number() == 0) return g;
				} catch (Exception e) {
					e.printStackTrace();
				}
				pre_node = g;
			}
		}
		throw new DataBrokenException();
	}
	
	// 終了した最新の試合の勝者を返す。
	synchronized private int get_last_winner() throws DataBrokenException, AllGameIsEndedException, IllegalTypeOfClassException{
		Queue<GameNode> q = new ArrayDeque<GameNode>();
		if( root.isEnded() ){
			try {
				throw new AllGameIsEndedException(root.get_game(root.get_winner_int()).get_winner().get_team());
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (GameNotEndedException e) {
				e.printStackTrace();
			}
		}
		q.add(root);
		while(!q.isEmpty()){
			GameNode g = q.remove();
			if(g.isGame()){
				// キューに子ノードを追加
				q.add(g.get_game(1));
				q.add(g.get_game(0));
				// 最新の試合かを確認
				try {
					if(g.isEnded() == true) return g.get_game(g.get_winner_int()).get_winner().get_team();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if(g.get_game_number() == 0) return -1;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		throw new DataBrokenException();
	}
	
	// まだ行われていない最新の試合をCurrentGameクラスで返す。
	synchronized CurrentGame get_current_game() throws DataBrokenException, AllGameIsEndedException, IllegalTypeOfClassException{
		GameNode g = get_current_gameNode();
		int game_id = 0;
		try {
			game_id = g.get_game_number();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int team0 = 0, team1 = 0;
		try {
			team0 = g.get_game(0).get_winner().get_team();
			team1 = g.get_game(1).get_winner().get_team();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CurrentGame(this, g, team0, team1, game_id, get_last_winner());
	}
	
	// トーナメントデータをXML形式で取得する。
	synchronized String get_xml(String version, String encoding){
		CodeXML xml = new CodeXML(version, encoding);
		xml.add_line(0, "<tournament>");
		root.get_xml(xml, 1);
		xml.add_line(0, "</tournament>");
		return xml.get_xml();
	}
	
	// 試合結果をint型配列で返すメソッド
	// -1     : 試合が行われていない
	// 0 or 1 : 勝者が0または1
	synchronized int[] get_result(){
		int[] ret = new int[this.num_team-1];
		Queue<GameNode> q = new ArrayDeque<GameNode>();
		q.add(root);
		while(!q.isEmpty()){
			GameNode g = q.remove();
			if(g.isGame()){
				// キューに子ノードを追加
				q.add(g.get_game(1));
				q.add(g.get_game(0));
				// 試合番号と試合結果の取得
				try {
					int num = g.get_game_number();
					try {
						ret[num] = g.get_winner_int();
					} catch (GameNotEndedException e) {
						ret[num] = -1;
					}
				} catch (IllegalTypeOfClassException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	private void calc_priority(){
		// -----------------------------
		// ラウンド数の決定
		for( num_round = 0; num_team > Math.pow(2, num_round); num_round++ );
//		System.out.println("There are " + num_round + " round(s)");
		
		// -----------------------------
		// 優先順位の決定
		// 第一ラウンドの試合数と優先順位の変数
		int number_of_game = (int)Math.pow(2, num_round-1);
		priority = new int[number_of_game];
		for(int i = 0; i < priority.length; i++){
			priority[i] = 0;// 0で初期化
		}
		
//		System.out.println("1st round has " + number_of_game + " game(s).");
//		System.out.println("delete " + (number_of_game*2 - num_team) + " game(s).");
		
		if( number_of_game*2 - num_team != 0 ){
			// 第1シードの指定
			priority[0] = 1;
			
			// 第2シード以降の決定
			int seed = 2;
			int seed_size = number_of_game;
			for(int i = 0; i < num_round-1; i++){
				// 計算済みかを判定
				if( number_of_game*2 <= (seed-1) + num_team ) break;
				
				for(int j = 0; j < Math.pow(2, i); j++){
					// 計算が必要かを判定
					if( number_of_game*2 <= (seed-1) + num_team ) break;
					
					// シードペア場所の検索
					int pair_place = 0;
					int pair_number =  (int)Math.pow(2, i+1) + 1 - seed;
					for(int k = 0; k < priority.length; k++){
						if(priority[k] == pair_number){
							pair_place = k;
							break;
						}
					}
					
					// シード位置の上限下限を決定
					int position = pair_place/seed_size;
					int lower = seed_size * position;
					int upper = seed_size * (position + 1);
					
					// シードペアから範囲内の最も遠い位置を検索
					int max_distance = 0;
					int max_place = 0;
					for(int k = lower; k != upper; k++){
						if(priority[k] == 0){ // 場所の優先度が未決定
							if(Math.abs(k-pair_place) > max_distance){
								max_distance = Math.abs(k-pair_place);
								max_place = k;
							}
						}
					}
					priority[max_place] = seed;
					
					// 次のシードへ
					seed++;
				}
				seed_size /= 2;
			}
		}
		/*
		// finish to decide priority;
		for(int i = 0; i < priority.length; i++){
			System.out.println(i + " : " + priority[i]);// 0で初期化
		}//*/
	}
	

}
