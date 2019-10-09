package game.pickup;

import game.GameObject;
import game.waves.Handler;

import java.awt.*;

/**
 *
 * @author Brandon Loehle 5/30/16
 * @author David Nguyen 12/13/17
 * 
 */

public class PickupScore extends GameObject {
	public PickupScore(Point.Double p, Handler handler) {
        super(p.x, p.y, 30, 30, handler);
	}

	@Override
	public void tick() {
	    super.tick();
	}
}