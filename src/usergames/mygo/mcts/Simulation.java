package usergames.mygo.mcts;

import usergames.mygo.GoGroupAIConfig;
import usergames.mygo.game.ScoreEvaluation;
import usergames.mygo.game.Board;
import usergames.mygo.game.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class is responsible for doing the play-outs.
 * If there are a lot of captures, the game could go on for quite some while.
 * To fasten the process, we abort after an fixed maximum amount of plays and evaluate the current state of the game.
 */
public class Simulation
{
	private static final int SAVE_MOVES = GoGroupAIConfig.getInstance().getInt("simulation_rave_moves");
	private static final int MAX_MOVES = GoGroupAIConfig.getInstance().getInt("simulation_max_moves");
	private final GameState gameState;

	private List<Move> simulatedMoves;

	public Simulation(GameState initialState)
	{
		this.gameState = new GameState(initialState);
	}

	public int simulate()
	{
		simulatedMoves = new ArrayList<Move>();

		// perform the simulation
		Random random = new Random();
		List<Move> possibleMoves;

		int i = 0;
		while (i < MAX_MOVES && (possibleMoves = gameState.getAvailableMoves(true)).size() > 0)
		{
			Move chosenMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
			gameState.play(chosenMove);
			if (i < SAVE_MOVES)
				simulatedMoves.add(chosenMove);
			i++;
		}

		// determine the winner
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(gameState);
		Board.Player winner = scoreEvaluation.getWinner();
		if (winner == Board.Player.Self)
			return 1;
		else if (winner == Board.Player.Opponent)
			return -1;
		else
			return 0;
	}

	/**
	 * Get the first few moves done in the simulation.
	 */
	public List<Move> getSimulatedMoves()
	{
		return simulatedMoves;
	}
}
