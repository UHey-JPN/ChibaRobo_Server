package data.exception;

public class AllGameIsEndedException extends Exception {

	private static final long serialVersionUID = 1L;
	private int last_winner;

	public AllGameIsEndedException(int last_winner) {
		super("All of game is ended.");
		this.last_winner = last_winner;
	}

	public AllGameIsEndedException(String message, int last_winner) {
		super("All of game is ended:" + message);
		this.last_winner = last_winner;
	}
	
	public int get_last_winner(){
		return last_winner;
	}

}
