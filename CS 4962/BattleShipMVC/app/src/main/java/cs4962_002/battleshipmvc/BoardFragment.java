package cs4962_002.battleshipmvc;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Melynda on 11/1/2014.
 */
public class BoardFragment extends Fragment
{
    private ArrayList<TileView> boardTiles;
    private ArrayList<TileView> shipTiles;
    private GridLayout clickBoard;
    private GridLayout shipBoard;
    private LinearLayout containBoard;
    private ImageView divider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        // Linear layout contains both boards.
        containBoard = new LinearLayout(getActivity());
        containBoard.setOrientation(LinearLayout.VERTICAL);
        containBoard.setBackgroundColor(Color.DKGRAY);
        containBoard.setPadding(10, 10, 10, 10);

        boardTiles = new ArrayList<TileView>();
        shipTiles = new ArrayList<TileView>();
        shipBoard = new GridLayout(getActivity());
        clickBoard = new GridLayout(getActivity());

        shipBoard.setColumnCount(10);
        shipBoard.setRowCount(10);
        clickBoard.setColumnCount(10);
        clickBoard.setRowCount(10);

        reset();

        return containBoard;
    }

    public interface OnTileTouchListener
    {
        public void onTileTouch(int row, int col, int inter);
    }

    public void set_onTileTouchListener(OnTileTouchListener listener)
    {
        _onTileTouchListener = listener;
    }

    private OnTileTouchListener _onTileTouchListener = null;

    /*
     *  Updates boards.
     */
    public void loadBoards(Boards boards)
    {
        if(boards == null || boards.playerBoard == null || boards.opponentBoard == null)
            return;

        shipBoard.removeAllViews();
        clickBoard.removeAllViews();
        containBoard.removeAllViews();

        // A divider to separate the boards.
        divider = new ImageView(getActivity());
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 10, 10);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.DKGRAY);

        // Load player board
        for(Tile t : boards.playerBoard)
        {
            TileView tv = new TileView(getActivity(), t.xPos, t.yPos);
            tv.setTileImage(t.status);
            shipBoard.addView(tv);
        }

        // Load opponent board
        for(Tile t : boards.opponentBoard)
        {
            TileView tv = new TileView(getActivity(), t.xPos, t.yPos);
            tv.setTileImage(t.status);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(_onTileTouchListener != null)
                    {
                        _onTileTouchListener.onTileTouch(((TileView) v).getRow(),
                                ((TileView) v).getCol(), ((TileView) v).getInteraction());
                    }
                }
            });

            clickBoard.addView(tv);
        }

        containBoard.addView(clickBoard, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        containBoard.addView(divider);
        containBoard.addView(shipBoard, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /*
     *  Builds boards.
     */
    public void reset()
    {
        int row = -1;
        int col = 0;
        TileView tv;

        shipBoard.removeAllViews();
        clickBoard.removeAllViews();
        containBoard.removeAllViews();

        // A divider to separate the boards.
        divider = new ImageView(getActivity());
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 10, 10);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.DKGRAY);

        // Create click board for player to launch missiles from.
        for(int i = 0; i < 100; i++) {
            col = (i + 10) % 10;
            if(col % 10 == 0)
                row++;

            tv = new TileView(getActivity(), row, col);
            clickBoard.addView(tv);
        }

        // Create ship board for player to view their own ship configuration.
        row = -1;
        for(int i = 0; i < 100; i++) {
            col = (i + 10) % 10;
            if (col % 10 == 0)
                row++;

            tv = new TileView(getActivity(), row, col);
            shipBoard.addView(tv);
        }

        containBoard.addView(clickBoard, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        containBoard.addView(divider);
        containBoard.addView(shipBoard, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        containBoard.setVisibility(View.VISIBLE);
        containBoard.invalidate();
    }
}
