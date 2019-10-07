package mainGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A type of enemy in the game
 * 
 * @author Brandon Loehle 5/30/16
 *
 */

public class EnemyShooterBullet extends GameObject {
	public EnemyShooterBullet(double x, double y, double velX, double velY, Handler handler) {
		super(x, y, 16, 16, handler);
		this.velX = velX;
		this.velY = velY;
	}

	public void tick() {
		this.x += velX;
		this.y += velY;

		// if (this.y <= 0 || this.y >= Game.HEIGHT - 40) velY *= -1;
		// if (this.x <= 0 || this.x >= Game.WIDTH - 16) velX *= -1;

        getHandler().add(new Trail(x, y, Color.yellow, (int)width/4, (int)width/4, 0.025, getHandler()));

		removeBullets();
	}

	private void removeBullets() {
		for (int i = 0; i < getHandler().size(); i++) {
			GameObject tempObject = getHandler().get(i);
			if (tempObject instanceof EnemyShooterBullet) {
				//check for removal
				if ((tempObject.getX() >= getHandler().getGameDimension().getWidth() || tempObject.getY() >= getHandler().getGameDimension().getHeight()) ||
					(tempObject.getX() < -100 || tempObject.getY() < -100)){
                    getHandler().remove(tempObject);
				}
			}

		}

	}

	public void render(Graphics g) {
		g.setColor(Color.red);
		Rectangle bounds = getBounds();
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	}
}
