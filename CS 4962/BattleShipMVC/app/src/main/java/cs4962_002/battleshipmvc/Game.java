package cs4962_002.battleshipmvc;

import java.util.Map;

/**
 * Created by Melynda on 11/16/2014.
 */

/*
 * Maintains the state fo the current game.
 */
public class Game
{
    private String gameId;
    private String gameName;
    private Player p1 = new Player();
    private Player p2 = new Player();
    private String winner;
    private String missiles;
    private Boolean player1Turn = false;

    // Singleton!
    static Game _instance = null;

    static Game getInstance()
    {
        // TODO: Make sure singleton is threadsafe.
        if(_instance == null) {
            _instance = new Game();
        }
        return _instance;
    }

    private Game(){}

    public void clear()
    {
        gameId = null;
        gameName = null;
        winner = null;
        missiles = "0";
        player1Turn = false;
    }

    public void setFields(Map<String, String> details)
    {
        gameId = details.get("id");
        gameName = details.get("name");
        setPlayerName(1, details.get("player1"));
        setPlayerName(2, details.get("player2"));
        winner = details.get("winner");
        missiles = details.get("misslesLaunched");
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getMissiles() {
        return missiles;
    }

    public void setMissiles(String missiles) {
        this.missiles = missiles;
    }

    public Boolean getPlayer1Turn() {
        return player1Turn;
    }

    public void setPlayer1Turn(Boolean player1Turn) {
        this.player1Turn = player1Turn;
    }

    public String getPlayerName(int playerNum)
    {
        if(playerNum == 1)
            return p1.name;
        else
            return p2.name;
    }

    public void setPlayerName(int playerNum, String n)
    {
        if(playerNum == 1)
            p1.name = n;
        else
            p2.name = n;
    }

    public String getPlayerId(int playerNum)
    {
        if(playerNum == 1)
            return p1.id;
        else
            return p2.id;
    }

    public void setPlayerId(int playerNum, String i)
    {
        if(playerNum == 1)
            p1.id = i;
        else
            p2.id = i;
    }

    private class Player
    {
        String name;
        String id;

        private Player(String n, String i)
        {
            name = n;
            id = i;
        }

        private Player()
        {
            name = "";
            id = "0";
        }
    }
}
