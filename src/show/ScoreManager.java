package show;

import java.util.Arrays;

class ScoreManager {
	private final String CRLF = "\r\n";
	private int[] score;

	public ScoreManager() {
		score = new int[2];
		this.reset();
	}
	
	public void reset(){
		score[0] = 0;
		score[1] = 0;
	}
	
	public void set_score(int id, int score) throws IllegalArgumentException{
		if( id == 0 || id == 1 ){
			this.score[id] = score;
			return;
		}
		throw new IllegalArgumentException();
	}
	
	public void set_all_score(int s0, int s1){
		score[0] = s0;
		score[1] = s1;
	}
	
	public String get_packet(){
		return new String( score[0] + "," + score[1] + CRLF );
	}

	public int[] get_point() {
		return Arrays.copyOf(score, score.length);
	}

}
