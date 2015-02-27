package cs4962_002.battleshipmvc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Melynda on 10/29/2014.
 */
public class TileView extends ImageView
{
    private Drawable image;
    private int interaction; // -1 hit, 0 nothing, 1 miss
    private boolean containsShip;
    private int row;
    private int col;

    public TileView(Context context, int r, int c)
    {
        super(context);
        interaction = 0;
        containsShip = false;
        row = r;
        col = c;
        image = getResources().getDrawable(R.drawable.battleship_water);
        setImageDrawable(image);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        setImageDrawable(image);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(43, 43);
    }

    public void setImage(int resid)
    {
        image = getResources().getDrawable(resid);
        setImageDrawable(image);
        invalidate();
    }

    public void setTileImage(Tile.Status status)
    {
        if(status == Tile.Status.HIT)
            setImage(R.drawable.battleship_hit);
        else if(status == Tile.Status.SHIP)
            setImage(R.drawable.battleship_shipbody);
        else if(status == Tile.Status.MISS)
            setImage(R.drawable.battleship_miss);
        else
            setImage(R.drawable.battleship_water);
    }

    public Drawable getImage()
    {
        return image;
    }

    public boolean isContainsShip() {
        return containsShip;
    }

    public void setContainsShip(boolean containsShip) {
        this.containsShip = containsShip;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getInteraction() {
        return interaction;
    }

    public void setInteraction(int interaction) {
        this.interaction = interaction;
    }
}