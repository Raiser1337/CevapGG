package usergames.mygo.game;

import usergames.mygo.cevapgg.GameState;

/**
 * The score evaluation for our own Go board.
 */
public class ScoreEvaluation
{
	private final Board board;

	public ScoreEvaluation(GameState gameState)
	{
		this.board = gameState.getBoard();
	}

	public ScoreEvaluation(usergames.mygo.mcts.GameState gameState) {
		this.board = gameState.getBoard();
	}

	public Board.Player getWinner()
	{
		int myScore = 0;
		int oppScore = 0;

		// count units
		for(int i = 0; i < board.getDimX(); i++)
		{
			for(int j = 0; j < board.getDimY(); j++)
			{
				if (board.getStone(i,j).getOwner() == Board.Player.Self)
				{
					myScore++;
				}
				else if (board.getStone(i,j).getOwner() == Board.Player.Opponent)
				{
					oppScore++;
				}
				else
				{
					if (board.isSurroundedBy(i, j, Board.Player.Self))
						myScore++;
					else if (board.isSurroundedBy(i, j, Board.Player.Opponent))
						oppScore++;
				}
			}
		}

		// based on the go game settings, captured stones count toward score or not
		/*
		myScore += board.getCapturedUnitCount(Board.Player.Self);
		oppScore += board.getCapturedUnitCount(Board.Player.Opponent);
		*/

		Board.Player winner = Board.Player.None;
		if (myScore > oppScore)
			winner = Board.Player.Self;
		else if (oppScore > myScore)
			winner = Board.Player.Opponent;

		return winner;
	}
}
