package usergames.mygo.cevapgg;

import usergames.mygo.GoGroupAIConfig;
import usergames.mygo.game.Board;
import usergames.mygo.game.Move;

import java.util.*;



public class Node
{
	private static final double UCT_CONST = 0.2;
	private static final double RAVE_WEIGHT_CONST = 128;


	private int gamesWon=0;
	private int gamesLost=0;
	private int gamesTied=0;
	private int gamesPlayed=0;
	
	private int gamesWonRAVE=0;
	private int gamesLostRAVE=0;
	private int gamesTiedRAVE=0;
	private int gamesPlayedRAVE=0;
	
	private Node parent;
	private Move simulatedMove;
	private GameState gameState;
	private Map<Move,Node> children;

	public Node(GameState gameState)
	{
		children = new HashMap<>();
		this.gameState = gameState;
	}

	public Node expand(Move move)
	{
		GameState newGameState = new GameState(gameState);
		newGameState.play(move);
		Node newNode = new Node(newGameState);
		newNode.parent = this;
		newNode.setSimulatedMove(move);
		this.children.put(move, newNode);
		return newNode;
	}

	public Map.Entry<Move,Node> selectChild(boolean isFinalSelection)
	{
		Set<Map.Entry<Move,Node>> set = children.entrySet();
		Iterator<Map.Entry<Move,Node>> iterator = set.iterator();
		double bestValue = -1;
		Map.Entry<Move,Node> chosenEntry = null;
		double totalSimulations = Math.log(gamesPlayed);
		double gameKnowledgeFactor = 1 - gameState.getBoard().getGameProgress();

		while (iterator.hasNext())
		{
			Map.Entry<Move,Node> entry = iterator.next();
			Node node=entry.getValue();
			Move move = entry.getKey();

			// calculate exploration
			double exploration = UCT_CONST * Math.sqrt(totalSimulations / (double)node.getGamesPlayed());

			// calculate exploitation
			double exploitationDirect = 0.5;
			if (node.getGamesPlayed() > 0){
				exploitationDirect = (double)node.getRelevantSize(entry.getKey().getPlayer()) / node.getGamesPlayed();
			}
			double exploitationRave = 0.5;
			double raveWeight = 0;
//			if (node.getRaveGamesPlayed() > 0){
//				exploitationRave = (double)node.getRelevantRaveSize(entry.getKey().getPlayer()) / node.getRaveGamesPlayed();				
//				raveWeight = (double)node.getRaveGamesPlayed() / (node.getGamesPlayed() + node.getRaveGamesPlayed() +
//						RAVE_WEIGHT_CONST * node.getGamesPlayed());
//			}
			
			//Criticality
			MoveStats stats = MCT.getMoveStat(move.plainHash());
			if (stats != null){
				exploitationRave = (double)node.getRelevantRaveSize(entry.getKey().getPlayer()) / node.getRaveGamesPlayed();
				int v = stats.getLostAgainst() + stats.getWonWith();
				int w = stats.getGamesPlayedWith();
				int b = stats.getGamesPlayedAgainst();
				int W = node.getRelevantRaveSize(entry.getKey().getPlayer());
				int N = node.getRaveGamesPlayed();
				int B = N - W; //DRAW GAMES MISSING
				
				raveWeight = v / node.getRaveGamesPlayed() - ( (w * W)/ N + (b * B)/ N );
			}
			

			double directWeight = 1 - raveWeight;
			double exploitation = directWeight * exploitationDirect + raveWeight * exploitationRave;

			// apply some game knowledge
			double captureAmplifier = 1.0 + (entry.getKey().getCapturedStones() * 0.10 * gameKnowledgeFactor);
			exploitation = exploitation * captureAmplifier;

			// calculate final value and compare with best value
			double nodeValue = (isFinalSelection) ? exploitation - exploration : exploitation + exploration;
			if (nodeValue > bestValue)
			{
				bestValue = nodeValue;
				chosenEntry = entry;
			}

			
		}

		return chosenEntry;
	}

	public int getRelevantSize(Board.Player player)
	{
		if (player == Board.Player.Self)
			return gamesWon;
		else
			return gamesLost;
	}
	
	public int getRelevantRaveSize(Board.Player player)
	{
		if (player == Board.Player.Self)
			return gamesWonRAVE;
		else
			return gamesLostRAVE;
	}

	public void updateStats(Score score)
	{
		
		switch(score){
		case Win:
			gamesPlayed++;
			gamesWon++;
			break;
		case Lose:
			gamesPlayed++;
			gamesLost++;
			break;
		case Draw:
			gamesPlayed++;
			gamesTied++;
			break;
		}

	}
	public void updateRave(Score score)
	{
		
		switch(score){
		case Win:
			gamesPlayedRAVE++;
			gamesWonRAVE++;
			break;
		case Lose:
			gamesPlayedRAVE++;
			gamesLostRAVE++;
			break;
		case Draw:
			gamesPlayedRAVE++;
			gamesTiedRAVE++;
			break;
		}

	}
	

	public Node getParent()
	{
		return parent;
	}

	public GameState getGameState()
	{
		return gameState;
	}

	public Map<Move,Node> getChildren()
	{
		return children;
	}


	public Move getSimulatedMove()
	{
		return simulatedMove;
	}

	public void setSimulatedMove(Move simulatedMove)
	{
		this.simulatedMove = simulatedMove;
	}
	public int getGamesPlayed(){
		return gamesPlayed;
	}
	public int getRaveGamesPlayed(){
		return gamesPlayedRAVE;
	}
}
