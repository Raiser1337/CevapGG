package usergames.mygo.mcts;

import usergames.mygo.game.Board;

/**
 * Holds the play-out statistics for a node in our tree.
 */
public class NodeStats
{
	private int gamesWon;
	private int gamesLost;
	private int gamesPlayed;

	public int getGamesWon()
	{
		return gamesWon;
	}

	public int getGamesLost()
	{
		return gamesLost;
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public int getRelevantSize(Board.Player player)
	{
		if (player == Board.Player.Self)
			return getGamesWon();
		else
			return getGamesLost();
	}

	public void update(int delta)
	{
		if (delta > 0)
		{
			addWin();
		}
		else if(delta < 0)
		{
			addLoss();
		}
		else
		{
			addTie();
		}
	}

	private void addWin()
	{
		gamesPlayed++;
		gamesWon++;
	}

	private void addLoss()
	{
		gamesPlayed++;
		gamesLost++;
	}

	private void addTie()
	{
		gamesPlayed++;
	}
}
