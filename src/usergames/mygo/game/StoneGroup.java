package usergames.mygo.game;

import java.util.ArrayList;
import java.util.List;

/**
 * If several stones from the same player are adjacent(connected), they represent a group and share their liberties.
 * Pseudo liberties (same liberties across several stones) are counted too.
 */
public class StoneGroup
{
	private int liberties;
	private final List<Stone> stones;

	public StoneGroup()
	{
		this.stones = new ArrayList<>();
	}

	public int getLiberties()
	{
		return liberties;
	}

	public void setLiberties(int liberties)
	{
		this.liberties = liberties;
	}

	public void decrementLiberties()
	{
		liberties--;
	}

	public void incrementLiberties()
	{
		liberties++;
	}

	public void addStone(Stone stone)
	{
		stones.add(stone);
		stone.setStoneGroup(this);
	}

	public List<Stone> getStones()
	{
		return stones;
	}

}
