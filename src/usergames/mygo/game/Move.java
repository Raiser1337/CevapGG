package usergames.mygo.game;

/**
 * A (possible) move of one of the two players.
 */
public class Move
{
	private final int x;
	private final int y;
	private final Board.Player player;

	private int capturedStones;

	public Move(int x, int y, Board.Player player)
	{
		this.x = x;
		this.y = y;
		this.player = player;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Board.Player getPlayer()
	{
		return player;
	}

	public int getCapturedStones()
	{
		return capturedStones;
	}

	public void setCapturedStones(int capturedStones)
	{
		this.capturedStones = capturedStones;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Move move = (Move) o;

		if (x != move.x) return false;
		if (y != move.y) return false;
		if (player != move.player) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = x;
		result = 31 * result + y;
		result = 31 * result + player.hashCode();
		return result;
	}
	
	public int plainHash()
	{
		int result = x;
		result = 31 * result + y;
		result = 31 * result;
		return result;
	}
}
