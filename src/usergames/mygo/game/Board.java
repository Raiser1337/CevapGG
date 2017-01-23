package usergames.mygo.game;

import games.strategy.engine.data.*;

import java.util.*;

/**
 * Our own representation of the Go board.
 */
public class Board
{
	private static final int TERRITORY_CHECK_MAX_FIELDS = 30;
	private final int dimX;
	private final int dimY;
	private int stoneCount;

	public enum Player
	{
		Self, Opponent, None
	}

	private final Stone[][] board;
	private final List<StoneGroup> stoneGroups;

	/**
	 * To keep track of captured units: capturedUnitCount[XY] are the units, that XY captured from his opponent.
	 */
	private final int[] capturedUnitCount;

	/**
	 * Create a new empty board.
	 * @param gameData The TripleA game data.
	 * @param playerID Our own player ID.
	 */
	public Board(GameData gameData, PlayerID playerID)
	{
		this.dimX = gameData.getMap().getXDimension();
		this.dimY = gameData.getMap().getYDimension();
		stoneCount = 0;
		stoneGroups = new ArrayList<>();
		capturedUnitCount = new int[2];
		board = new Stone[dimX][dimY];
		for (int i = 0; i < dimX; i++)
		{
			for (int j = 0; j < dimY; j++)
			{
				board[i][j] = new Stone(i,j,Player.None);
			}
		}

		// place already set stones on our board
		for (Territory t : gameData.getMap().getTerritories())
		{
			if (!t.getUnits().isEmpty())
			{
				Unit u = (Unit)t.getUnits().getUnits().toArray()[0];
				setStone(t.getX(), t.getY(), getPlayerFromOwner(u.getOwner(), playerID));
			}
		}
	}

	/**
	 * Create a new board copying the state from another board.
	 * This method is used when expanding a node in our tree. All references have to be set to new objects.
	 * @param copyFrom The board to copy the state from.
	 */
	public Board(Board copyFrom)
	{
		this.dimX = copyFrom.dimX;
		this.dimY = copyFrom.dimY;
		this.board = new Stone[dimX][dimY];
		this.stoneCount = copyFrom.stoneCount;
		for (int i = 0; i < this.board.length; i++)
		{
			for (int j = 0; j < this.board[i].length; j++)
			{
				this.board[i][j] = new Stone(copyFrom.board[i][j]);
			}
		}
		this.capturedUnitCount = Arrays.copyOf(copyFrom.capturedUnitCount, 2);

		// recreate groups
		stoneGroups = new ArrayList<>();
		for(StoneGroup group : copyFrom.stoneGroups)
		{
			StoneGroup clonedGroup = new StoneGroup();
			stoneGroups.add(clonedGroup);
			clonedGroup.setLiberties(group.getLiberties());

			for(Stone stone : group.getStones())
			{
				clonedGroup.addStone(board[stone.getX()][stone.getY()]);
				board[stone.getX()][stone.getY()].setStoneGroup(clonedGroup);
			}
		}
	}

	/**
	 * Set a stone at a specific position on the board and apply capture logic.
	 */
	public void setStone(int x, int y, Player player)
	{
		board[x][y].setOwner(player);
		stoneCount++;

		StoneGroup newGroup = new StoneGroup();
		stoneGroups.add(newGroup);
		newGroup.addStone(board[x][y]);

		Set<StoneGroup> adjGroups = new HashSet<>();
		List<Stone> neighbours = getNeighbours(x,y);

		// decrements liberties for all neighbours & find existing adjacent groups from the same player
		for(Stone neighbour : neighbours)
		{
			neighbour.getStoneGroup().decrementLiberties();
			if (neighbour.getOwner() == player)
				adjGroups.add(neighbour.getStoneGroup());
			else if (neighbour.getOwner() == Player.None)
				newGroup.incrementLiberties();
		}
		// merge existing adjacent groups from the same player to the new group
		for(StoneGroup group : adjGroups)
		{
			newGroup.setLiberties(newGroup.getLiberties() + group.getLiberties());
			for(Stone stone :group.getStones())
			{
				newGroup.addStone(stone);
			}
			stoneGroups.remove(group);
		}
		// remove stones from board
		Set<StoneGroup> groupsToRemove = new HashSet<>();
		for(Stone neighbour : neighbours)
		{
			if (neighbour.getStoneGroup().getLiberties() == 0)
				groupsToRemove.add(neighbour.getStoneGroup());
		}
		for (StoneGroup groupToRemove : groupsToRemove)
		{
			capturedUnitCount[player.ordinal()]++;
			for(Stone stone : groupToRemove.getStones())
			{
				board[stone.getX()][stone.getY()].setOwner(Player.None);
				board[stone.getX()][stone.getY()].setStoneGroup(new StoneGroup());
				stoneCount--;
			}
			stoneGroups.remove(groupToRemove);
		}
	}

	/**
	 * Get the stone for the specified coordinates.
	 */
	public Stone getStone(int x, int y)
	{
		return board[x][y];
	}

	/**
	 * Get the x-dimension of the board.
	 */
	public int getDimX()
	{
		return dimX;
	}

	/**
	 * Get the y-dimension of the board.
	 */
	public int getDimY()
	{
		return dimY;
	}

	/**
	 * Get the neighbours for a specific field.
	 */
	public List<Stone> getNeighbours(int x, int y)
	{
		List<Stone> neighbours = new ArrayList<>();
		if (x >= 1)
			neighbours.add(board[x - 1][y]);
		if (y >= 1)
			neighbours.add(board[x][y - 1]);
		if (x < dimX - 1)
			neighbours.add(board[x + 1][y]);
		if (y < dimY - 1)
			neighbours.add(board[x][y + 1]);
		return neighbours;
	}

	/**
	 * Check if a field is empty.
	 */
	public boolean isEmptyField(int x, int y)
	{
		return board[x][y].getOwner() == Player.None;
	}

	/**
	 * Check if a field is already surrounded by units of a specific player.
	 */
	public boolean isSurroundedBy(int x, int y, Player player)
	{
		for(Stone stone : getNeighbours(x, y))
		{
			if (stone.getOwner() != player)
				return false;
		}
		return true;
	}

	/**
	 * Check if a field is already in the territory of someone.
	 */
	public boolean isTerritoryOf(int x, int y, Player player)
	{
		return isTerritoryOf(x, y, player, new HashSet<Stone>());
	}

	/**
	 * Use an algorithm similar to flood fill to check whose territory a field is.
	 */
	private boolean isTerritoryOf(int x, int y, Player player, Set<Stone> visitedStones)
	{
		Stone currentStone = getStone(x, y);
		if (currentStone.getOwner() == player)
			return true;
		if (visitedStones.contains(currentStone))
			return true;
		if (visitedStones.size() > TERRITORY_CHECK_MAX_FIELDS)
			return false;

		visitedStones.add(currentStone);
		boolean result = true;
		for(Stone neighbour : getNeighbours(x, y))
		{
			if (neighbour.getOwner() == getOtherPlayer(player))
				result = false;
			else if (neighbour.getOwner() == Player.None)
				result = result & isTerritoryOf(neighbour.getX(), neighbour.getY(), player, visitedStones);
		}
		return result;
	}

	/**
	 * Get the amount of captured Stones we would get with a move at pos (x,y).
	 * Important: The board state has to remain unchanged (temporary changes have to be reverted).
	 * @param x The x coordinate of our Move.
	 * @param y The y coordinate of our Move.
	 * @param player The player to make the Move.
	 * @return The amount of captured Stones or a negative number, if it would be suicide.
	 */
	public int getPossibleCapture(int x, int y, Player player)
	{
		List<Stone> neighbours = getNeighbours(x,y);
		Set<StoneGroup> allAdjGroups = new HashSet<>();
		StoneGroup newGroup = new StoneGroup(); // a dummy group to count the liberties

		// simulate setting the stone
		for(Stone neighbour : neighbours)
		{
			neighbour.getStoneGroup().decrementLiberties();
			if (neighbour.getOwner() == Player.None)
				newGroup.incrementLiberties();
			else
				allAdjGroups.add(neighbour.getStoneGroup());
		}
		for(StoneGroup group : allAdjGroups)
		{
			if (group.getStones().get(0).getOwner() == player)
				newGroup.setLiberties(newGroup.getLiberties() + group.getLiberties());
		}

		// check if there are groups without liberties
		int capturedOwnStones = 0;
		int capturedOppStones = 0;
		for(StoneGroup group : allAdjGroups)
		{
			if (group.getLiberties() == 0)
			{
				List<Stone> stones = group.getStones();
				if (stones != null && stones.size() > 0)
				{
					if (stones.get(0).getOwner() == getOtherPlayer(player))
						capturedOppStones += stones.size();
				}
			}
		}
		if (newGroup.getLiberties() == 0)
			capturedOwnStones = 1;

		// revert changes on the board
		for(Stone neighbour : neighbours)
		{
			neighbour.getStoneGroup().incrementLiberties();
		}

		// its suicide if we have to remove our own stones, but none of our opponents
		if (capturedOppStones == 0 && capturedOwnStones > 0)
			return -capturedOwnStones;
		else
			return capturedOppStones;
	}

	/**
	 * Get the amount of units, the player has captured from his opponent.
	 */
	public int getCapturedUnitCount(Player player)
	{
		return capturedUnitCount[player.ordinal()];
	}

	/**
	 * Get information about the game progress to tell, if we are in the opening, middle or end phase of the game.
	 * @return	A double value, representing the percentage of the board, that is occupied.
	 * 			0 represents an empty board, 1 a completely filled board.
	 */
	public double getGameProgress()
	{
		int size = getDimY() * getDimX();
		return stoneCount / (double)size;
	}

	/**
	 * Helper method to convert the TripleA PlayerId to our own Board.Player data type.
	 */
	private Board.Player getPlayerFromOwner(PlayerID owner, PlayerID playerId)
	{
		if (owner == playerId)
			return Board.Player.Self;
		else
			return Board.Player.Opponent;
	}

	/**
	 * Helper method to quickly get the player different to the specified player.
	 */
	public static Player getOtherPlayer(Player player)
	{
		return (player == Player.Self) ? Player.Opponent : Player.Self;
	}
}
