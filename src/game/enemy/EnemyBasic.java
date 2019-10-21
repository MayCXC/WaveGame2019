package game.enemy;

import game.GameLevel;
import game.GameObject;
import game.Player;

/**
 *
 * @author Brandon Loehle 5/30/16
 *
 */

public class EnemyBasic extends GameObject.Bouncing {
	public EnemyBasic(GameLevel level) {
		super(level.spawnPoint(), 125, 60, level);
		setVelX(10 * (level.getRandom().random() < .5 ? -1 : 1));
		setVelY(10 * (level.getRandom().random() < .5 ? -1 : 1));
	}

    @Override
    public void collide(Player p) {
        p.damage(2);
    }
}
