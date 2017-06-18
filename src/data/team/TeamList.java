package data.team;

import java.util.ArrayList;
import java.util.List;

import data.exception.DataNotFoundException;
import data.main.CodeXML;

public class TeamList {
	private List<Team> list;
	private final Object lock_obj = new Object();

	public TeamList() {
		// list = Collections.synchronizedList(new ArrayList<Team>());
		list = new ArrayList<Team>();
		this.add(1, "default_name_team1", 0, 1, "default_desc_team1");
		this.add(2, "default_name_team2", 10, 11, "default_desc_team2");
	}
	
	public boolean add(int id, String name, int id1, int id2, String desc){
		synchronized(lock_obj){
			// 重複を削除
			for(int i = 0; i < list.size(); i++){
				if( list.get(i).get_id() == id ){
					list.remove(list.get(i));
					i--;
				}
			}
			
			// 新規データを追加
			list.add(new Team(id, name, id1, id2, desc));
			return list.get(list.size()-1).get_valid();
		}
	}
	
	public void clear(){
		synchronized(lock_obj){
			list.clear();
		}
	}
	
	public int[] get_robot_id(int team_id) throws DataNotFoundException {
		synchronized(lock_obj){
			for(Team t : list){
				if(t.get_id() == team_id){
					return t.get_robot_id();
				}
			}
			throw new DataNotFoundException("Team(id=" + team_id + ") is not found.");
		}
	}
	
	public String get_team_summary(int team_id) throws DataNotFoundException{
		synchronized(lock_obj){
			for(Team t : list){
				if(t.get_id() == team_id){
					return t.get_summary();
				}
			}
			throw new DataNotFoundException("Team(id=" + team_id + ") is not found.");
		}
	}
	
	// スレッドセーフなメソッド
	// XML形式でロボットの情報を取得
	public String get_xml(){
		return get_xml("1.0", "utf-8");
	}
	public String get_xml(String encoding){
		return get_xml("1.0", encoding);
	}
	public String get_xml(String version, String encoding){
		CodeXML xml = new CodeXML(version, encoding);
		xml.add_line(0, "<teamList>");
		ArrayList<Team> copy;
		synchronized(lock_obj){
			copy = new ArrayList<Team>(list);
		}
		for(Team r : copy){
			if(r.get_valid())
				r.create_xml(xml);
		}
		xml.add_line(0, "</teamList>");
		return xml.get_xml();
	}

}
