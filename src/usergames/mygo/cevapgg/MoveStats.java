package usergames.mygo.cevapgg;

import usergames.mygo.game.Board.Player;
import usergames.mygo.game.Board;
import usergames.mygo.game.Move;

public class MoveStats {
	
	private int gamesPlayedWith;
	private int gamesPlayedAgainst;
	private int wonWith;
	private int lostAgainst;
	private Move move;
	private boolean partOfSim;
	
	public MoveStats (Move move){
		this.move = move;
	}
	
	public void addMove (Move move){
		partOfSim=true;
		if(move.getPlayer().equals(Player.Self)){
			gamesPlayedWith++;
		} else {
			gamesPlayedAgainst++;
		}
	}
	
	public void addResult (Player winner){
		if(!partOfSim)
			return;
		
		if(winner == Player.Self){
			wonWith++;
		} else if (winner == Board.Player.Opponent) {
			lostAgainst++;
		}
		
		partOfSim = false;
	}

	public int getGamesPlayedWith() {
		return gamesPlayedWith;
	}

	public int getGamesPlayedAgainst() {
		return gamesPlayedAgainst;
	}

	public int getWonWith() {
		return wonWith;
	}

	public int getLostAgainst() {
		return lostAgainst;
	}
	
	

}
