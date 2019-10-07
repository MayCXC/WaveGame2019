package mainGame;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Waves extends GameMode {
	private int currentLevelNum = 1;
	private Level currentLevel = null;
    private boolean paused = false;
    private Handler handler;
    private HUD hud;
    private Player player;
    private GameState upgradeScreen;
    private GameState menu;
    private GameState gameOver;
    private Upgrades upgrades;

	public Level getCurrentLevel() {
	    return currentLevel;
    }
    public void setPaused(boolean p) {
        paused = p;
    }
    public Handler getHandler() {
        return handler;
    }
    public HUD getHUD() {
        return hud;
    }
    public Player getPlayer() {
        return player;
    }
    public GameState getUpgradeScreen() {
        return upgradeScreen;
    }
    public GameState getMenu() {
        return menu;
    }
    public GameState getGameOver() {
        return gameOver;
    }
    public Upgrades getUpgrades() {
        return upgrades;
    }

    private boolean africa = false;
    public void setMenuMusic(boolean a) {
        // Toggle menu theme between Space Jam and Africa
        africa = a;
        // Restart menu music
        AudioUtil.closeMenuClip();
        AudioUtil.playMenuClip(true, africa);
    }

    private static class RandomDifferentElement<T> {
        private java.util.List<T> source;
        private int lastIndex;
        public RandomDifferentElement(List<T> s) {
            source = s;
            lastIndex = -1;
        }
        @SafeVarargs
        public RandomDifferentElement(T... s) {
            this(Arrays.asList(s));
        }
        public T next() {
            if(source.size() == 0) {
                return null;
            }
            else if(source.size() == 1) {
                return source.get(0);
            }
            int l = lastIndex;
            while(l == lastIndex) {
                l = (int)(source.size()*Math.random());
            }
            lastIndex = l;
            return source.get(l);
        }
    }

    private RandomDifferentElement<Function<Point, GameObject>> randomEasyEnemy = new RandomDifferentElement<>(
        p -> new EnemyBasic(p.getX(), p.getY(), 9, 9, getHandler()),
        p -> new EnemyBurst(-200, 200, 15, 15, 200,
            new String[]{ "left", "right", "top", "bottom" }[(int)(4*Math.random())],
            getHandler()),
        p -> new EnemyFast(p.getX(), p.getY(), getHandler()),
        p -> new EnemyShooter(p.getX(), p.getY(), 100, 100, -20 + (int)(Math.random()*5), getHandler()),
        p -> new EnemySmart(p.getX(), p.getY(), -5, getHandler())
    );

    private RandomDifferentElement<Function<Point, GameObject>> randomHardEnemy = new RandomDifferentElement<>(
        p -> new EnemyShooterMover(p.getX(), p.getY(), 100, 100, -20 + (int)(Math.random()*5), getHandler()),
        p -> new EnemyShooterSharp(p.getX(), p.getY(), 200, 200, -20 + (int)(Math.random()*5), getHandler()),
        p -> new EnemySweep(p.getX(), p.getY(), 9, 2, getHandler())
    );

    private RandomDifferentElement<Supplier<GameObject>> randomBoss = new RandomDifferentElement<>(
        () -> new EnemyBoss(getHandler(),currentLevelNum/10, getHUD()),
        () -> new EnemyRocketBoss(100,100, getPlayer(), getHandler(), getHUD(), this,currentLevelNum/10)
//      () -> new BossEye(0, 0, ID.BossEye, getHandler(), 0)
    );

    public Waves(Dimension screenSize) {
        handler = new Handler(screenSize, player);
        menu = new Menu(this);
        hud = new HUD(this);

        setState(menu);

        player = new Player(screenSize.getWidth() / 2 - 32, screenSize.getHeight() / 2 - 32, this);
        upgradeScreen = new UpgradeScreen(this);
        upgrades = new Upgrades(this);
        gameOver = new GameOver(this);
    }

    /**
     * Used to switch between each of the screens shown to the user
     */

    public enum GAME_AUDIO {
        Menu, Game, None
    }

	/**
	 * Ticks Level classes generated.
	 * Generates levels when they are completed. 
	 */
    public GAME_AUDIO gameCurrentClip = GAME_AUDIO.Menu;

    @Override
	public void tick() {
        if (this.paused) {return;}

		if(currentLevel==null || !currentLevel.running()) {
            getHandler().clearEnemies();
            getHandler().clearPickups();
            getHUD().setLevel(this.currentLevelNum);

            if (currentLevelNum > 1 && currentLevelNum%5 == 1 && getState() != upgradeScreen) { // upgrade after every boss
                setState(upgradeScreen);
            }
            else if(this.currentLevelNum%5 == 0) {
                System.out.println("New Boss Level");
                currentLevel = new Level( this,60*(20), currentLevelNum, 1,  p -> randomBoss.next().get());
                currentLevelNum += 1;
            }
            else {
                System.out.println("New Normal Level");
                RandomDifferentElement<Function<Point,GameObject>> randomEnemy = new RandomDifferentElement<>(
                    IntStream.rangeClosed(0, Math.min(5,currentLevelNum/5))
                        .mapToObj( i -> i > 3 && Math.random() > .5
                            ? randomHardEnemy.next()
                            : randomEasyEnemy.next()
                        )
                        .collect(Collectors.toList())
                );

                currentLevel = new Level( this,60*(20), currentLevelNum, currentLevelNum,  p -> randomEnemy.next().apply(p));

                Point.Double pd = new Point.Double(
                    (Math.random()*(getHandler().getGameDimension().getWidth()-300))+150,
                    (Math.random()*(getHandler().getGameDimension().getHeight()-300))+150
                );

                if(currentLevelNum > 1) {
                    getHandler().addPickup(
                        new RandomDifferentElement<BiFunction<Point.Double, Handler, GameObject>>(
                            PickupFreeze::new,
                            PickupHealth::new,
                            PickupLife::new,
                            PickupScore::new,
                            PickupSize::new
                        ).next().apply(pd, getHandler())
                    );
                }
                currentLevelNum += 1;
            }
		}

		getState().tick();
        if (getState() == menu) { // user is on menu, update the menu items
            if (this.gameCurrentClip != GAME_AUDIO.Menu) {
                this.gameCurrentClip = GAME_AUDIO.Menu;
                AudioUtil.closeGameClip();
                AudioUtil.playMenuClip(true, false);
            }
        }
        if (getState() == currentLevel) { // game is running
            if (this.gameCurrentClip != GAME_AUDIO.Game) {
                this.gameCurrentClip = GAME_AUDIO.Game;
                AudioUtil.closeMenuClip();
                AudioUtil.playGameClip(true);
            }
            hud.tick();
        }
    }

	/**
	 * Renders any static images for the level.
	 * IE Background. 
	 */
	@Override
	public void render(Graphics g) {
	    Image img = getHandler().getTheme().get(getClass());

        if(img != null) {
            g.drawImage(img, 0, 0, (int) getHandler().getGameDimension().getWidth(), (int) getHandler().getGameDimension().getHeight(), null);
        }

        getState().render(g);
        if(getState() == currentLevel) {
            hud.render(g);
        }
	}

    /**
	 * @param hardReset - if false only enemies are wiped. If true gamemode is completely reset. 
	 */
	@Override
	public void resetMode(boolean hardReset) {
		this.currentLevel = null;
        getHandler().clearEnemies();
		if(hardReset) {
			this.currentLevelNum = 1;
			getPlayer().setWidth(32);
			getPlayer().setHeight(32);
			getHUD().setExtraLives(0);
			getHUD().resetHealth();
		}
	}

	public void resetMode() {
		resetMode(true);
	}
}
