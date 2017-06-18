package data.main;

import data.exception.AllGameIsEndedException;
import data.exception.DataBrokenException;
import data.exception.DataNotFoundException;
import data.exception.IllegalTypeOfClassException;
import data.robot.RoboList;
import data.team.TeamList;
import data.tournament.CurrentGame;
import data.tournament.Tournament;

public class Database {
	private final String CRLF = "\r\n";
	private RoboList robolist;
	private TeamList teamlist;
	private Tournament tour;
	
	private CurrentGame current_game;

	public Database(int num_team) {
		this.robolist = new RoboList();
		this.teamlist = new TeamList();
		this.tour = new Tournament(num_team);
		try {
			this.get_current_game();
		} catch (DataBrokenException | AllGameIsEndedException e) {
			e.printStackTrace();
		}
		try {
			current_game = tour.get_current_game();
		} catch (DataBrokenException | AllGameIsEndedException | IllegalTypeOfClassException e) {
			e.printStackTrace();
		}
	}
	
	public void reset_tournament(int num_team){
		this.tour = new Tournament(num_team);
	}
	
	public int get_num_of_team(){
		return tour.get_num_of_team();
	}
	
	public void set_team_number_list(String data) throws IllegalArgumentException{
		String[] splited = data.split(",");
		int[] integer = new int[splited.length];
		for(int i = 0; i < splited.length; i++){
			integer[i] = Integer.parseInt(splited[i]);
		}
		this.set_team_number_list(integer);
	}
	public void set_team_number_list(int[] data) throws IllegalArgumentException{
		tour.set_team_number(data);
		try {
			this.get_current_game();
		} catch (DataBrokenException | AllGameIsEndedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean add_robolist(int id, String data){
		return robolist.add(id, data);
	}
	public void clear_robolist(){
		robolist.clear();
	}
	
	public void add_teamlist(int id, String name, int id1, int id2, String desc){
		teamlist.add(id, name, id1, id2, desc);
	}
	public void clear_teamlist(){
		teamlist.clear();
	}
	
	public String get_xml_robolist(){
		return robolist.get_xml();
	}
	public String get_xml_robolist(String encoding){
		return robolist.get_xml(encoding);
	}

	public String get_xml_teamlist(){
		return teamlist.get_xml();
	}
	public String get_xml_teamlist(String encoding){
		return teamlist.get_xml(encoding);
	}
	
	public String get_xml_tournament(){
		return tour.get_xml();
	}
	public String get_xml_tournament(String encoding){
		return tour.get_xml(encoding);
	}
		
	public boolean all_game_isEnded(){
		return tour.all_game_isEnded();
	}
	
	private CurrentGame get_current_game() throws DataBrokenException, AllGameIsEndedException{
		try {
			this.current_game = tour.get_current_game();
		} catch (IllegalTypeOfClassException e) {
			e.printStackTrace();
		}
		return this.current_game;
	}
	public CurrentGame get_current_game_no_calc(){
		return this.current_game;
	}
	
	public String get_tournament_result(){
		return tour.get_result();
	}
	
	public CurrentGame set_winner(int winner) throws DataBrokenException, AllGameIsEndedException{
		current_game.set_winner(winner);
		try {
			current_game = tour.get_current_game();
		} catch (AllGameIsEndedException e) {
			this.current_game = CurrentGame.AllEndedCurrentGame(e.get_last_winner());
			throw new AllGameIsEndedException(e.get_last_winner());
		} catch (IllegalTypeOfClassException e) {
			e.printStackTrace();
		}
		return current_game;
	}
	
	public int get_last_winner(){
		return current_game.get_last_winner();
	}

	public String get_vs_teams() {
		String ret = "";
		for(int i = 0; i < 2; i++){
			try {
				int[] robot_id = teamlist.get_robot_id(current_game.get_team_id(i));
				ret += teamlist.get_team_summary(current_game.get_team_id(i)) + CRLF;
				for(int j = 0; j < 2; j++){
					try {
						ret += robolist.get_robot_summary(robot_id[j]) + CRLF;
					} catch (DataNotFoundException e) {
						// e.printStackTrace();
						ret += "No data_robo" + CRLF;
					}
				}
			} catch (DataNotFoundException e1) {
				e1.printStackTrace();
				ret += "No data_team" + CRLF;
				ret += "No data_robo" + CRLF;
				ret += "No data_robo" + CRLF;
			}
		}
		return ret;
	}
	

}
