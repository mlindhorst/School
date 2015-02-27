package cs4962_002.battleshipmvc;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Melynda on 11/13/2014.
 */
public class BattleShipNetwork
{
    public static final String BASE_URL = "http://battleship.pixio.com";

    // Request: GET /api/games/:id
    // Returns: id, name, player1, player2, winner, missles
    public static Map<String, String> retrieveGame(String gameIdentifier) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(BASE_URL + "/api/games/" + gameIdentifier);
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Gson gson = new Gson();
            Map<String, String> details = gson.fromJson(responseString, Map.class);

            return details;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: GET /api/games
    // Returns: id, name, status
    public static GameSpecs[] retrieveGameList() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(BASE_URL + "/api/games");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Gson gson = new Gson();
            GameSpecs[] gameList = gson.fromJson(responseString, GameSpecs[].class);

            return gameList;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: POST /api/games/:id/join
    // Returns: playerID
    public static Map<String, String> joinGame(String gameIdentifier, String playerName) {
        try {
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("playerName", playerName);
            Gson gson = new Gson();
            String payloadString = gson.toJson(payload);

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(BASE_URL + "/api/games/" + gameIdentifier + "/join");
            request.setEntity(new StringEntity(payloadString));
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Map<String, String> playerID = gson.fromJson(responseString, Map.class);

            return playerID;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: POST /api/games
    // Returns: playerId, gameId
    public static Map<String, String> newGame(String gameName, String playerName) {
        try {
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("gameName", gameName);
            payload.put("playerName", playerName);
            Gson gson = new Gson();
            String stringPayload = gson.toJson(payload);

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(BASE_URL + "/api/games");
            request.setEntity(new StringEntity(stringPayload));
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Map<String, String> playerID = gson.fromJson(responseString, Map.class);

            return playerID;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: POST /api/games/:id/guess
    // Returns: hit, shipSunk
    public static Map<String, String> launchMissile(String gameIdentifier, String playerID, int x, int y) {
        try {
            Missile m = new Missile();
            m.playerId = playerID;
            m.xPos = x;
            m.yPos = y;
            Gson gson = new Gson();
            String stringPayload = gson.toJson(m);

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(BASE_URL + "/api/games/" + gameIdentifier + "/guess");
            request.setEntity(new StringEntity(stringPayload));
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Map<String, String> stuff = gson.fromJson(responseString, Map.class);

            return stuff;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: POST /api/games/:id/status
    // Returns: isYourTurn, winner
    public static TurnInfo whoseTurn(String gameIdentifier, String playerID) {
        try {
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("playerId", playerID);
            Gson gson = new Gson();
            String stringPayload = gson.toJson(payload);

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(BASE_URL + "/api/games/" + gameIdentifier + "/status");
            request.setEntity(new StringEntity(stringPayload));
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            TurnInfo turn = gson.fromJson(responseString, TurnInfo.class);

            return turn;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Request: POST /api/games/:id/board
    // Returns: playerBoard, opponentBoard
    public static Boards getBoards(String gameIdentifier, String playerID) {
        try {
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("playerId", playerID);
            Gson gson = new Gson();
            String stringPayload = gson.toJson(payload);

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(BASE_URL + "/api/games/" + gameIdentifier + "/board");
            request.setEntity(new StringEntity(stringPayload));
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);

            InputStream responseContent = response.getEntity().getContent();
            Scanner responseScanner = new Scanner(responseContent).useDelimiter("\\A"); // Read everything.
            String responseString = responseScanner.hasNext() ? responseScanner.next() : null;

            if(responseString == null)
                return null;

            Boards boards = gson.fromJson(responseString, Boards.class);

            return boards;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}