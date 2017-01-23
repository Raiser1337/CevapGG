package usergames.mygo;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.grid.go.delegate.PlayDelegate;
import games.strategy.grid.go.delegate.remote.IGoPlayDelegate;
import games.strategy.grid.player.GridAbstractAI;
import games.strategy.grid.ui.GridPlayData;
import games.strategy.grid.ui.IGridPlayData;
import usergames.mygo.cevapgg.MCT;
import usergames.mygo.game.Move;
import usergames.mygo.mcts.MCTreeSearch;
import usergames.mygo.cevapgg.GameState;

public class CevapGG extends GridAbstractAI{
	
	private MCT mct;
	
	public CevapGG(String name, String type)
	{
		super(name, type);
	}

	@Override
	protected void play()
	{
		
		pause();
		IGoPlayDelegate playDelegate = (IGoPlayDelegate) getPlayerBridge().getRemoteDelegate();
		GameData data = getGameData();
		mct = new MCT(new GameState(data, getPlayerID()));
		String result = playWithMCTS(data, getPlayerID(), playDelegate);

	}

	private String playWithMCTS(GameData data, PlayerID playerId, IGoPlayDelegate playDelegate)
	{
			MCT mcTreeSearch = new MCT(new GameState(data, playerId));
			mcTreeSearch.search();

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
