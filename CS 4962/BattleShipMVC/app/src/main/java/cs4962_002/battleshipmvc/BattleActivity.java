package cs4962_002.battleshipmvc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class BattleActivity extends Activity {
    private FrameLayout playerLayout;
    private BoardFragment player;
    private MenuFragment gameList;
    private FragmentTransaction ft;
    private Boolean gameStarted = false;
    private Boolean myTurn = false;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final LinearLayout baseLayout = new LinearLayout(this);
        baseLayout.setOrientation(LinearLayout.VERTICAL);

        // Game Banner
        TextView gameBanner = new TextView(this);
        gameBanner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        gameBanner.setTextColor(Color.WHITE);
        gameBanner.setBackgroundColor(Color.BLUE);
        gameBanner.setText("Welcome to BattleShip");

        // New Game Button
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        Button startNew = new Button(this);
        startNew.setTextSize(12.0f);
        startNew.setMinHeight(0);
        startNew.setHighlightColor(Color.LTGRAY);
        startNew.setBackgroundColor(Color.BLUE);
        startNew.setTextColor(Color.WHITE);
        startNew.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 50));
        startNew.setText("New Game");
        startNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
        buttonLayout.addView(startNew);

        // Fragments
        final LinearLayout gameScreenLayout = new LinearLayout(this);
        gameScreenLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Board Fragment
        playerLayout = new FrameLayout(this);
        player = new BoardFragment();
        playerLayout.setId(14);

        // Game List Fragment
        FrameLayout menuLayout = new FrameLayout(this);
        gameList = new MenuFragment();
        menuLayout.setId(15);

        ft = getFragmentManager().beginTransaction();
        ft.add(14, player);
        ft.add(15, gameList);
        ft.commit();

        player.set_onTileTouchListener(new BoardFragment.OnTileTouchListener() {
            @Override
            public void onTileTouch(int row, int col, int inter) {
                Log.i("TURN INFO", "Launched missile!");
                launchMissile(row, col);
                checkGameState();
                myTurn = false;
            }
        });
        gameList.setOnGameSelectedListener(new MenuFragment.OnGameSelectedListener() {
            @Override
            public void onGameSelected(MenuFragment menuFragment, String name) {
                checkGameState();
            }
        });
        gameList.setOnGameListChangedListener(new MenuFragment.OnGameListChangedListener() {
            @Override
            public void onGameListChanged() {
                findViewById(15).invalidate();
                gameScreenLayout.invalidate();
            }
        });

        gameScreenLayout.addView(menuLayout, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
        gameScreenLayout.addView(playerLayout, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 70));

        baseLayout.addView(gameBanner);
        baseLayout.addView(buttonLayout);
        baseLayout.addView(gameScreenLayout);

        promptPlayerName();

        setContentView(baseLayout);
    }

    //regionGame Methods
    /*
     *  Prompts users for game name and launches new game.
     */
    public void newGame()
    {
        Log.i("TURN INFO", "New game started!");
        Game.getInstance().clear();
        promptGameName();
    }

    /*
     *  Queries server for new game information.
     */
    public void launchNewGame(){
        AsyncTask<String, Integer, String> newGameTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                Map<String, String> ids =  BattleShipNetwork.newGame(Game.getInstance().getGameName(), Game.getInstance().getPlayerName(1));

                Game.getInstance().setGameId(ids.get("gameId"));
                Game.getInstance().setPlayerId(1, ids.get("playerId"));

                return null;
            }

            @Override
            protected void onPostExecute(String ids) {
                super.onPostExecute(ids);

                checkGameState();
            }
        };
        newGameTask.execute();
    }

    /*
     *  Queries server for game detail information. Not currently using this method.
     */
    public void getGameDetails() {
        AsyncTask<String, Integer, String> getDetailsTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                Map<String, String> details = BattleShipNetwork.retrieveGame(Game.getInstance().getGameId());

                Game.getInstance().setFields(details);

                return null;
            }

            @Override
            protected void onPostExecute(String details) {
                super.onPostExecute(details);
            }
        };
        getDetailsTask.execute();
    }

    /*
     *  Launches timer and game state checks.
     */
    private void checkGameState() {
        if(Game.getInstance().getGameId().length() == 0 || Game.getInstance().getPlayerId(1).length() == 0) {
            Log.i("Is it my turn?", "Unable to start timer.");
            return;
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                myTurn();
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);

            }
        }, 0, 3000);
    }

    /*
     *  Performs game state checks and chooses appropriate actions based on game state.
     */
    public void myTurn() {
        if(Game.getInstance().getGameId() == null)
            return;

        AsyncTask<String, Integer, TurnInfo> turnTask = new AsyncTask<String, Integer, TurnInfo>() {
            @Override
            protected TurnInfo doInBackground(String... params) {
                TurnInfo response = BattleShipNetwork.whoseTurn(Game.getInstance().getGameId(),
                        Game.getInstance().getPlayerId(1));
                return response;
            }

            @Override
            protected void onPostExecute(TurnInfo turnInfo) {
                super.onPostExecute(turnInfo);

                Boolean turn = turnInfo.isYourTurn;
                String winner = turnInfo.winner;

                if(turn && myTurn)
                    return;

                refreshBoards();

                if(!winner.equals("IN PROGRESS")) {
                    myTurn = false;
                    gameStarted = false;
                    cancelTimer();
                    refreshBoards();

                    String win = "Winner: " + winner;
                    Log.i("TURN INFO", win);
                    toastTime(win);
                }
                else if(turn && !gameStarted) {
                    if(timer != null)
                        cancelTimer();

                    refreshBoards();
                    gameStarted = true;
                }
                else if(!turn && !gameStarted) {
                    toastTime("Waiting...");
                    Log.i("TURN INFO", "Waiting for a player to join.");
                }
                else if(myTurn != turn) {
                    refreshBoards();
                    myTurn = turn;
                    if(myTurn) {
                        Log.i("TURN INFO", "It's your turn.");
                        cancelTimer();
                    }
                }
                else {
                    toastTime("Waiting...");
                    Log.i("TURN INFO", "Waiting for opponent.");
                }
            }
        };

        turnTask.execute();
    }

    /*
     *  Cancels timer.
     */
    public void cancelTimer() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /*
     *  Queries server for new board configuations.
     */
    public void refreshBoards()
    {
        AsyncTask<String, Integer, Boards> getBoardsTask = new AsyncTask<String, Integer, Boards>() {
            @Override
            protected Boards doInBackground(String... params) {
                Boards tiles = BattleShipNetwork.getBoards(Game.getInstance().getGameId(),
                        Game.getInstance().getPlayerId(1));
                return tiles;
            }

            @Override
            protected void onPostExecute(Boards tiles) {
                super.onPostExecute(tiles);

                player.loadBoards(tiles);
                playerLayout.invalidate();
            }
        };
        getBoardsTask.execute();
    }

    /*
     *   Queries server to launch a missile and receive return information for parsing.
     */
    public void launchMissile(final int xp, final int yp)
    {
        AsyncTask<String, Integer, String> launchMissileTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                Map<String, String> results = BattleShipNetwork.launchMissile(Game.getInstance().getGameId(),
                        Game.getInstance().getPlayerId(1), xp, yp);
                return null;
            }

            @Override
            protected void onPostExecute(String tiles) {
                super.onPostExecute(tiles);
            }
        };
        launchMissileTask.execute();
    }
    //endregion

    //regionUser I/O Methods
    public void promptPlayerName()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Player Name");
        alert.setMessage("Please enter your name:");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Game.getInstance().setPlayerName(1, value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Game.getInstance().setPlayerName(1, "null");
            }
        });

        alert.show();
    }

    public void promptGameName()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Game Title");
        alert.setMessage("Please enter the game title:");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Game.getInstance().setGameName(value);
                launchNewGame();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Game.getInstance().setGameName("game");
            }
        });

        alert.show();
    }

    public void toastTime(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    //endregion
}