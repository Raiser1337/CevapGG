package usergames.mygo.cevapgg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import usergames.mygo.GoGroupAIConfig;
import usergames.mygo.game.Board;
import usergames.mygo.game.Move;
import usergames.mygo.game.ScoreEvaluation;
import usergames.mygo.cevapgg.GameState;
import usergames.mygo.cevapgg.Node;
import usergames.mygo.cevapgg.Simulation;



public class MCT {
	private Random rand;
	private Node root;
	private static Map<Integer, MoveStats> moveStatsMap;
	
	private static final int AVAILABLE_TIME = 2000;

	public MCT(GameState s0)
	{
		root = new Node(s0);
		moveStatsMap = new HashMap<Integer, MoveStats>();
	}

	public void search()
	{
		long searchStartedAt = System.currentTimeMillis();
		rand = new Random(searchStartedAt);

		int simCount = 0;

		while (searchStartedAt+AVAILABLE_TIME > System.currentTimeMillis())
		{
			Node selectedNode = selectNode(root);
			Simulation simulation = new Simulation(selectedNode.getGameState(), moveStatsMap);
			Score score = simulation.simulate();
			simCount++;
			updateStats(selectedNode, score, simulation.getSimulatedMoves());
		}

	}

	/**
	 * Take the path with the currently most promising looking child.
	 */
	public Move chooseMove()
	{
		Map.Entry<Move,Node> chosenChild = root.selectChild(true);
		if (chosenChild == null){
			return null;
		}
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
	private Node selectNode(Node node)
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
	private void updateStats(Node node, Score score, List<Move> simulatedMoves)
	{
		while (node != null)
		{
			Map<Move, Node> children = node.getChildren();
			for(Move simulateMove : simulatedMoves)
			{
				Node nodeWithMove = children.get(simulateMove);
				if (nodeWithMove != null)
				{
					nodeWithMove.updateRave(score);
				}
			}
			simulatedMoves.add(node.getSimulatedMove());
			node.updateStats(score);
			node = node.getParent();
		}
		
	}
	
	public static MoveStats getMoveStat(int moveHash){
		return moveStatsMap.get(moveHash);
	}
	
}
