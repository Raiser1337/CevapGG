package usergames.mygo;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.grid.go.delegate.PlayDelegate;
import games.strategy.grid.go.delegate.remote.IGoPlayDelegate;
import games.strategy.grid.player.GridAbstractAI;
import games.strategy.grid.ui.GridPlayData;
import games.strategy.grid.ui.IGridPlayData;
import usergames.mygo.mcts.GameState;
import usergames.mygo.game.Move;
import usergames.mygo.mcts.MCTreeSearch;

/**
 * The AI Implementation from our group.
 *
 * It handles the communication between the TripleA engine and our MCTS implementation.
 *
 * @author Go Group (group name)
 * @author Aigner Andreas (0800304)
 * @author Berger Christoph (1129111)
 * @author Wolfsberger Lukas (1100986)
 */
public class GoGroupAI extends GridAbstractAI
{
	public GoGroupAI(String name, String type)
	{
		super(name, type);
	}

	@Override
	protected void play()
	{
		// simulate pause for thinking
		pause();

		// perform the move
		IGoPlayDelegate playDelegate = (IGoPlayDelegate) getPlayerBridge().getRemoteDelegate();
		GameData data = getGameData();
		String result = playWithMCTS(data, getPlayerID(), playDelegate);

		// An error has occurred
		if (result != null)
		{
			System.out.println(String.format("ERROR (%s): %s", getPlayerID(), result));
		}
	}

	/**
	 * Run the Monte Carlo Tree Search and fetch the most promising child.
	 * We do not fully check if the chosen move might be an earlier game state (and therefore invalid).
	 * If a chosen move is invalid, we simply take the next one.
	 */
	private String playWithMCTS(GameData data, PlayerID playerId, IGoPlayDelegate playDelegate)
	{
		MCTreeSearch mcTreeSearch = new MCTreeSearch(new GameState(data, playerId));
		mcTreeSearch.doSearch();

		IGridPlayData play = null;
		Move chosenMove;
		do
		{
			chosenMove = mcTreeSearch.chooseMove();
			mcTreeSearch.removeChild(chosenMove);
			if (chosenMove != null)
			{
				play = createPlayFromMove(data, playerId, chosenMove);
			}
		} while (chosenMove != null && PlayDelegate.isValidPlay(play, playerId, data) != null);

		if (chosenMove != null)
			return playDelegate.play(play);

		// there is no useful move - just pass
		return playDelegate.play(new GridPlayData(true, playerId));
	}

	/**
	 * Create a TripleA play data from our Move.
	 */
	private IGridPlayData createPlayFromMove(GameData data, PlayerID playerId, Move move)
	{
		Territory destination = data.getMap().getTerritoryFromCoordinates(move.getX(), move.getY());
		return new GridPlayData(destination, playerId);
	}
}
