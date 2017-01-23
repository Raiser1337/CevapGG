package usergames.mygo;

import games.strategy.engine.gamePlayer.IGamePlayer;
import games.strategy.grid.go.Go;
import games.strategy.grid.go.player.GoPlayer;
import games.strategy.grid.go.player.RandomAI;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GoWithAI extends Go
{
	private static final String HUMAN_PLAYER_TYPE = "Human";
	private static final String GO_COMPUTER_PLAYER_TYPE = "GO Group AI";
	private static final String GO_RANDOM_COMPUTER_PLAYER_TYPE = "Random AI";
	private static final String CEVAPGG_TYPE ="CevapGG AI";

	@Override
	public String[] getServerPlayerTypes()
	{
		return new String[] { HUMAN_PLAYER_TYPE, GO_RANDOM_COMPUTER_PLAYER_TYPE, GO_COMPUTER_PLAYER_TYPE, CEVAPGG_TYPE };
	}

	@Override
	public Set<IGamePlayer> createPlayers(Map<String, String> playerNames)
	{
		Set<IGamePlayer> playerSet = new HashSet<>();
		for (String name : playerNames.keySet())
		{
			String type = playerNames.get(name);
			switch (type)
			{
				case HUMAN_PLAYER_TYPE:
				case CLIENT_PLAYER_TYPE:
					final GoPlayer player = new GoPlayer(name, type);
					playerSet.add(player);
					break;
				case GO_RANDOM_COMPUTER_PLAYER_TYPE:
					final RandomAI randomAI = new RandomAI(name, type);
					playerSet.add(randomAI);
					break;
				case GO_COMPUTER_PLAYER_TYPE:
				{
					final GoGroupAI goGroupAI = new GoGroupAI(name, type);
					playerSet.add(goGroupAI);
					break;
				}
				case CEVAPGG_TYPE:
					final CevapGG cevapGGAI = new CevapGG(name, type);
					playerSet.add(cevapGGAI);
					break;
				default:
					throw new IllegalStateException("Player type not recognized:" + type);
			}
		}
		return playerSet;
	}
}
