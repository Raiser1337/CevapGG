package usergames.mygo.mcts;

import usergames.mygo.GoGroupAIConfig;
import usergames.mygo.game.Move;

import java.util.*;

/**
 * The implementation of the nodes in our tree.
 * Every node has its own GameState (and within it a board representation) and play-out statistics.
 */
public class Node
{
	private static final double UCT_CONST = 0.2;
	private static final double RAVE_WEIGHT_CONST = 128;

	private final NodeStats stats;
	private final NodeStats raveStats;

	private Node parent;
	private Move simulatedMove;
	private GameState gameState;
	private Map<Move,Node> children;

	public Node(GameState gameState)
	{
		children = new HashMap<>();
		stats = new NodeStats();
		raveStats = new NodeStats();
		this.gameState = gameState;
	}

	/**
	 * Expand this node.
	 * @param move The move of the player.
	 * @return The created node.
	 */
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

	/**
	 * Select the best move for the active player.
	 * @return the node who returns the highest value for our evaluation function with UCT and RAVE
	 */
	public Map.Entry<Move,Node> selectChild(boolean isFinalSelection)
	{
		Set<Map.Entry<Move,Node>> set = children.entrySet();
		Iterator<Map.Entry<Move,Node>> iterator = set.iterator();

		boolean printDebug = GoGroupAIConfig.getInstance().getBoolean("print_debug");
		double bestValue = -1;
		Map.Entry<Move,Node> chosenEntry = null;
		double totalSimulations = Math.log(stats.getGamesPlayed());

		// the earlier in the game, the higher we value the game knowledge
		double gameKnowledgeFactor = 1 - gameState.getBoard().getGameProgress();

		// iterate over possible candidates
		while (iterator.hasNext())
		{
			Map.Entry<Move,Node> entry = iterator.next();
			NodeStats nodeStats = entry.getValue().getStats();
			NodeStats raveStats = entry.getValue().getRaveStats();

			// calculate exploration
			double exploration = UCT_CONST * Math.sqrt(totalSimulations / (double)nodeStats.getGamesPlayed());

			// calculate exploitation
			double exploitationDirect = 0.5;
			if (nodeStats.getGamesPlayed() > 0)
				exploitationDirect = (double)nodeStats.getRelevantSize(entry.getKey().getPlayer()) / nodeStats.getGamesPlayed();

			double exploitationRave = 0.5;
			if (raveStats.getGamesPlayed() > 0)
				exploitationRave = (double)raveStats.getRelevantSize(entry.getKey().getPlayer()) / raveStats.getGamesPlayed();

			double raveWeight = 0;
			if (raveStats.getGamesPlayed() > 0)
			{
				raveWeight = (double)raveStats.getGamesPlayed() / (nodeStats.getGamesPlayed() + raveStats.getGamesPlayed() +
						RAVE_WEIGHT_CONST * nodeStats.getGamesPlayed());
			}

			double directWeight = 1 - raveWeight;
			double exploitation = directWeight * exploitationDirect + raveWeight * exploitationRave;

			// apply some game knowledge
			double captureAmplifier = 1.0 + (entry.getKey().getCapturedStones() * 0.05 * gameKnowledgeFactor);
			exploitation = exploitation * captureAmplifier;

			// calculate final value and compare with best value
			double nodeValue = (isFinalSelection) ? exploitation - exploration : exploitation + exploration;
			if (nodeValue > bestValue)
			{
				bestValue = nodeValue;
				chosenEntry = entry;
			}

			// print some information
			if (isFinalSelection && printDebug)
				printEntryInformation(entry, exploitation, exploration, nodeValue, directWeight * exploitationDirect, raveWeight * exploitationRave);
		}

		if (isFinalSelection && chosenEntry != null && printDebug)
		{
			System.out.println("-----------------------------------------------------");
			System.out.println(String.format("Picking move at (%s,%s) with value %.3f",
					chosenEntry.getKey().getX(), chosenEntry.getKey().getY(), bestValue));
			System.out.println("-----------------------------------------------------");
		}
		return chosenEntry;
	}

	/** print some information about the entry for debugging purposes */
	private void printEntryInformation(Map.Entry<Move, Node> entry, double exploitation, double exploration, double nodeValue,
									   double directValue, double raveValue)
	{
		NodeStats stats = entry.getValue().getStats();
		System.out.println(String.format("Move at (%s,%s)", entry.getKey().getX(), entry.getKey().getY()));
		System.out.println(String.format("won: %s  lost: %s  played: %s (r: %.3f)",
				stats.getGamesWon(), stats.getGamesLost(), stats.getGamesPlayed(), stats.getGamesWon() / (double)stats.getGamesPlayed()));
		System.out.println(String.format("Exploitation: %.3f (%.3f + %.3f)  Exploration: %.3f  Overall: %.3f",
				exploitation, directValue, raveValue, exploration, nodeValue));
		System.out.println();
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

	public NodeStats getStats()
	{
		return stats;
	}

	public NodeStats getRaveStats()
	{
		return raveStats;
	}

	public Move getSimulatedMove()
	{
		return simulatedMove;
	}

	public void setSimulatedMove(Move simulatedMove)
	{
		this.simulatedMove = simulatedMove;
	}
}
