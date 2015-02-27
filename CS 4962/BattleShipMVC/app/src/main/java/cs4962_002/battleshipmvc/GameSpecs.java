package cs4962_002.battleshipmvc;

/**
 * Created by Melynda on 11/16/2014.
 */

/*
 * POJO - Holds game detail information from server.
 */
public class GameSpecs
{
    private String id;
    private String name;
    private Status status;

    public GameSpecs()
    {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status
    {
        DONE, WAITING, PLAYING
    }
}
