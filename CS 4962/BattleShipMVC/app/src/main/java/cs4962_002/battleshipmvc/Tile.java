package cs4962_002.battleshipmvc;

/**
 * Created by Melynda on 11/18/2014.
 */
public class Tile
{
    public int xPos;
    public int yPos;
    public Status status;

    public Tile(){}

    public enum Status
    {
        HIT, MISS, SHIP, NONE
    }
}
