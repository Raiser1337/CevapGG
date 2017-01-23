package usergames.mygo.cevapgg;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import usergames.mygo.game.Board;
import usergames.mygo.game.Move;
import usergames.mygo.game.Stone;

import java.util.ArrayList;
import java.util.List;

/**
 * A GameState holds the state of the board and other relevant information at one specific moment.
 * Each node in our tree has exactly one game state.
 */
public class GameState
{
	/** the player who is the next to do a move */
	private Board.Player activePlayer;
	/** our representation of the go board */
	private final Board board;
	/** the available moves for the current board state.
	 * (gets "invalid" when doing a play and refreshed on the next request) */
	private List<Move> availableMoves;

	/**
	 * Create a new game state based upon a TripleA game data.
	 * @param gameData The TripleA game data.
	 * @param playerID Our own player ID.
	 */
	public GameState(GameData gameData, PlayerID playerID)
	{
		availableMoves = null;
		activePlayer = Board.Player.Self;
		board = new Board(gameData, playerID);
	}

	/**
	 * Create a new game state copying its objects from an already existing state.
	 * @param copyFrom The game state we want to use as model.
	 */
	public GameState(GameState copyFrom)
	{
		availableMoves = null;
		activePlayer = copyFrom.activePlayer;
		board = new Board(copyFrom.board);
	}

	/**
	 * Perform a play (move) on this game state.
	 * @param move The move.
	 */
	public void play(Move move)
	{
		availableMoves = null;
		board.setStone(move.getX(), move.getY(), move.getPlayer());
		switchActivePlayers();
	}

	/**
	 * Find all possibly useful and allowed moves for the current player.
	 * @return A list with the moves.
	 */
	public List<Move> getAvailableMoves(boolean useFastMode)
	{
		if (availableMoves == null)
		{
			availableMoves = new ArrayList<>();
			for (int i = 0; i < board.getDimX(); i++)
			{
				for (int j = 0; j < board.getDimY(); j++)
				{
					if (!board.isEmptyField(i, j))
						continue;
					if (board.isSurroundedBy(i, j, activePlayer))
					{
						if (useFastMode)
							continue;

						boolean stoneInDanger = false;
						boolean enoughLiberties = false;
						for(Stone stone : board.getNeighbours(i,j))
						{
							if (stone.getStoneGroup().getLiberties() == 1)
							{
								for (Stone neighbour : board.getNeighbours(stone.getX(), stone.getY()))
								{
									if (neighbour.getOwner() != activePlayer)
										stoneInDanger = true;
								}
							}
							// 3 pseudo liberties from actual field + 2 additional surrounded fields (a 4 pseudo) = 11 liberties
							if (stone.getStoneGroup().getLiberties() >= 11)
							{
								enoughLiberties = true;
							}
						}

						if (!(stoneInDanger && enoughLiberties))
							continue;
					}

					int captStones = board.getPossibleCapture(i, j, activePlayer);
					if (captStones >= 1 || (captStones == 0 && (useFastMode || !board.isTerritoryOf(i, j, Board.getOtherPlayer(activePlayer)))))
					{
						Move newMove = new Move(i, j, activePlayer);
						newMove.setCapturedStones(captStones);
						availableMoves.add(newMove);
					}
				}
			}
		}
		return availableMoves;
	}

	private void switchActivePlayers()
	{
		activePlayer = Board.getOtherPlayer(activePlayer);
	}

	public Board getBoard()
	{
		return board;
	}
}
