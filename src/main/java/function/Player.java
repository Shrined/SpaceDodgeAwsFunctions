package function;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private String score;

    public Player(String name, String score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getScore() {
        return score;
    }
}
