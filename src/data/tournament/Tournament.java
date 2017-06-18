package data.tournament;

import data.exception.AllGameIsEndedException;
import data.exception.DataBrokenException;
import data.exception.IllegalTypeOfClassException;

public class Tournament {
	private Game game_data;
	private int num_team;

	public Tournament(int num) {
		this.num_team = num;
		game_data = new Game(num);
		game_data.calc_game_number();
		
		// チーム番号の仮決定
		int[] team = new int[this.num_team];
		for(int i = 0; i < this.num_team; i++){
			team[i] = i;
		}
		this.set_team_number(team);
	}

	// トーナメント表内のチーム番号を設定するメソッド
	public void set_team_number(int[] team_list) throws IllegalArgumentException{
		if(team_list.length != num_team)
			throw new IllegalArgumentException("not match the number of team.");
		game_data.set_team(team_list);
	}
	
	// 最新の試合を取得する。
	public CurrentGame get_current_game() 
			throws DataBrokenException, AllGameIsEndedException, IllegalTypeOfClassException{
		return game_data.get_current_game();
	}
	
	// 全試合が終了しているかをboolean型で返すメソッド
	public boolean all_game_isEnded(){
		try {
			get_current_game();
		} catch (AllGameIsEndedException e) {
			return true;
		} catch (DataBrokenException | IllegalTypeOfClassException e) {
			// e.printStackTrace();
			// Nothing to do.
		}
		return false;
	}
	
	// トーナメントの進行状況と試合結果をString型で返す。
	public String get_result(){
		int[] result = game_data.get_result();
		String ret = "" + result[0];
		for(int i = 1; i < result.length; i++){
			ret += "," + result[i];
		}
		return ret;
	}
	
	public String get_xml(){
		return get_xml("1.0", "utf-8");
	}
	public String get_xml(String encoding){
		return get_xml("1.0", encoding);
	}
	public String get_xml(String version, String encoding){
		return game_data.get_xml(version, encoding);		
	}

	public int get_num_of_team() {
		return this.num_team;
	}
}
