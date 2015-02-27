package cs4962_002.battleshipmvc;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Melynda on 11/1/2014.
 */
public class MenuFragment extends Fragment implements ListAdapter
{
    public interface OnGameSelectedListener
    {
        public void onGameSelected(MenuFragment menuFragment, String name);
    }
    private OnGameSelectedListener onGameSelectedListener = null;

    public interface OnGameListChangedListener
    {
        public void onGameListChanged();
    }
    private OnGameListChangedListener onGameListChangedListener = null;

    private String[] gamesByName;
    private TextView activeView;
    private ListView gameListView;
    private GameSpecs[] gamesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        gameListView = new ListView(getActivity());
        gameListView.setAdapter(this);
        gameListView.setBackgroundColor(Color.CYAN);
        refreshGameList();

        return gameListView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        if(gamesList == null)
            return 0;
        else
            return gamesList.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(gamesByName == null || position > gamesByName.length - 1)
        {
            //refreshGameList();
            gameListView.invalidate();
        }

        String gameIdentity = gamesList[position].getName();
        TextView gameTitleView = new TextView(getActivity());
        gameTitleView.setTextSize(10.0f);
        gameTitleView.setText(gameIdentity);

        gameTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gameName = ((TextView) v).getText().toString();
                activeView = (TextView)v;
                // TODO: Set selected game as playable game.

                for(GameSpecs gs : gamesList)
                {
                    if(gs.getName() == activeView.getText())
                    {
                        if(gs.getStatus() == GameSpecs.Status.WAITING) {
                            joinGame(gs.getId());
                            Game.getInstance().setGameId(gs.getId());
                            if(onGameSelectedListener != null)
                                onGameSelectedListener.onGameSelected(MenuFragment.this, gameName);
                        }
                        // TODO: Let player return to a game to finish.
                    }
                }
            }
        });

        gameListView.invalidateViews();

        return gameTitleView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() > 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    public void setOnGameSelectedListener(OnGameSelectedListener listener)
    {
        onGameSelectedListener = listener;
    }

    public void setOnGameListChangedListener(OnGameListChangedListener onGameListChangedListener) {
        this.onGameListChangedListener = onGameListChangedListener;
    }

    public void refreshGameList()
    {
        //TODO: Refresh cache asynchronously
        AsyncTask<String, Integer, GameSpecs[]> listGamesTask = new AsyncTask<String, Integer, GameSpecs[]>() {
            @Override
            protected GameSpecs[] doInBackground(String... params) {
                GameSpecs[] gs =  BattleShipNetwork.retrieveGameList();
                return gs;
            }

            @Override
            protected void onPostExecute(GameSpecs[] games) {
                super.onPostExecute(games);

                gamesList = games;
                gameListView.invalidateViews();

                if(onGameListChangedListener != null)
                    onGameListChangedListener.onGameListChanged();
            }
        };
        listGamesTask.execute();
    }

    public void joinGame(final String gameId)
    {
        AsyncTask<String, Integer, String> joinGameTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                Map<String, String> id = BattleShipNetwork.joinGame(gameId, Game.getInstance().getPlayerName(1));
                Game.getInstance().setPlayerId(1, id.get("playerId"));
                return null;
            }

            @Override
            protected void onPostExecute(String id) {
                super.onPostExecute(id);
                Game.getInstance().setGameId(gameId);
            }
        };
        joinGameTask.execute();
    }
}
