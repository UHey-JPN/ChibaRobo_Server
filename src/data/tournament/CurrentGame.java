package data.tournament;

public class CurrentGame {
	private GameNode game;
	private int[] team = new int[2];
	private int game_id;
	private Game manager;
	private int last_winner;

	public CurrentGame(Game manager, GameNode game, int team0, int team1, int game_id, int last_winner) {
		this.manager = manager;
		this.game = game;
		this.team[0] = team0;
		this.team[1] = team1;
		this.game_id = game_id;
		this.last_winner = last_winner;
	}

	public static CurrentGame AllEndedCurrentGame(int last_winner) {
		Game manager = new Game(2);
		GameNode game = new GameNode(new GameNode(-1), new GameNode(-1));
		return new CurrentGame(manager, game, -1, -1, -1, last_winner);
	}

	// データの同期を行うためGameクラスのインスタンスから試合結果を設定
	public void set_winner(int winner){
		manager.set_winner(game, winner);
	}
	
	public int get_game_id(){ return game_id; }
	public int get_team_id(int id){ return team[id]; }
	public int get_last_winner(){ return last_winner; }

}
