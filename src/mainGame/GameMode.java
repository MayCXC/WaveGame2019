package mainGame;

import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;

/**
 * @author Team B3
 * @author Aaron Paterson 9/12/19
 */
public abstract class GameMode extends MouseAdapter implements Animatable {
    private GameState state;
    public void setState(GameState s) {
        state = s;
    }
    public GameState getState() {
        return state;
    }
    abstract void resetMode();
	abstract GameObject getEnemyFromID(ID x, Point spawnLoc);
	abstract void resetMode(boolean hardReset);
	abstract KeyListener getKeyInput();
}
