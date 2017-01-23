package usergames.mygo.mcts;

import usergames.mygo.GoGroupAIConfig;
import usergames.mygo.game.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Our implementation of the Monte Carlo Tree Search.
 * We are trying the different branches of our tree as long, as we are within the time limit (set via property file).
 */
public class MCTreeSearch
{
	private Random rand;
	private Node root;

	private static final int AVAILABLE_TIME = GoGroupAIConfig.getInstance().getInt("mcts_time");
	private static final int MINIMUM_SIMULATIONS = GoGroupAIConfig.getInstance().getInt("mcts_minimum_simulations");

	public MCTreeSearch(GameState s0)
	{
		root = new Node(s0);
	}

	/**
	 * Perform the Monte Carlo Tree Search until the time is up.
	 */
	public void doSearch()
	{
		long searchStartedAt = System.currentTimeMillis();
		rand = new Random(searchStartedAt);

		// process the tree as long as possible
		int simCount = 0;
		while (isWithinTimeLimit(searchStartedAt, AVAILABLE_TIME) || simCount < MINIMUM_SIMULATIONS) // added minimum Simulations for easier debugging
		{
			Node v1 = doSelection(root);

			Simulation simulation = new Simulation(v1.getGameState());
			int delta = simulation.simulate();

			simCount++;
			doBackpropagation(v1, delta, simulation.getSimulatedMoves());
		}

		if (GoGroupAIConfig.getInstance().getBoolean("print_debug"))
			System.out.println(root.getStats().getGamesWon() + root.getStats().getGamesLost() + " Games simulated.");
	}

	/**
	 * Take the path with the currently most promising looking child.
	 */
	public Move chooseMove()
	{
		Map.Entry<Move,Node> chosenChild = root.selectChild(true);
		if (chosenChild == null)
			return null;
		return chosenChild.getKey();
	}

	/**
	 * Remove one of our direct children.
	 */
	public void removeChild(Move move)
	{
		if (root.getChildren().containsKey(move))
			root.getChildren().remove(move);
	}

	/**
	 * Select and expand the next node.
	 */
	private Node doSelection(Node node)
	{
		List<Move> availableMoves;
		while ((availableMoves = node.getGameState().getAvailableMoves(false)).size() > 0)
		{
			// there are moves available -> the game is not over
			Map<Move,Node> children = node.getChildren();
			if (children.size() != availableMoves.size())
			{
				// do Expansion
				List<Move> untriedMoves = new ArrayList<>(availableMoves);
				untriedMoves.removeAll(children.keySet());
				Move chosenMove = untriedMoves.get(rand.nextInt(untriedMoves.size()));
				return node.expand(chosenMove);
			}
			else
			{
				// we have already expanded all nodes in this level - choose a child and search there for expansion candidates
				node = node.selectChild(false).getValue();
			}
		}
		return node;
	}

	/**
	 * Propagate the results back up to the root.
	 */
	private void doBackpropagation(Node node, int delta, List<Move> simulatedMoves)
	{
		while (node != null)
		{
			// update RAVE statistics
			Map<Move, Node> children = node.getChildren();
			for(Move simulateMove : simulatedMoves)
			{
				Node nodeWithMove = children.get(simulateMove);
				if (nodeWithMove != null)
				{
					nodeWithMove.getRaveStats().update(delta);
				}
			}
			simulatedMoves.add(node.getSimulatedMove());

			// update common statistics
			node.getStats().update(delta);
			node = node.getParent();
		}
	}

	/**
	 * Check if we are within our time limit.
	 */
	private boolean isWithinTimeLimit(long searchStartedAt, int timeAvailable)
	{
		return (searchStartedAt + timeAvailable > System.currentTimeMillis());
	}
}
