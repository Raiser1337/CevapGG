package usergames.mygo.game;

/**
 * Represents one stone (unit) from one of the two players or an empty field (stone with owner == None).
 */
public class Stone
{
	private final int x;
	private final int y;
	private Board.Player owner;
	private StoneGroup stoneGroup;

	public Stone(int x, int y, Board.Player owner)
	{
		this.x = x;
		this.y = y;
		this.setOwner(owner);
		this.stoneGroup = new StoneGroup();
	}

	public Stone(Stone copyFrom)
	{
		this.x = copyFrom.x;
		this.y = copyFrom.y;
		this.setOwner(copyFrom.getOwner());
		this.stoneGroup = new StoneGroup();
	}

	public Board.Player getOwner()
	{
		return owner;
	}

	public void setOwner(Board.Player owner)
	{
		this.owner = owner;
	}

	public StoneGroup getStoneGroup()
	{
		return stoneGroup;
	}

	public void setStoneGroup(StoneGroup stoneGroup)
	{
		this.stoneGroup = stoneGroup;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

}
