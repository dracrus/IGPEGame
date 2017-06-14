package it.unical.igpe.ai;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;

import it.unical.igpe.game.World;
import it.unical.igpe.logic.Bullet;
import it.unical.igpe.logic.Enemy;
import it.unical.igpe.logic.Tile;
import it.unical.igpe.tools.TileType;
import it.unical.igpe.tools.Updatable;

public class EnemyManager implements Updatable {
	static LinkedList<Enemy> ens;
	public LinkedList<Bullet> bls;
	private World world;
	private Astar astar;
	private boolean[][] map;

	public EnemyManager(World _world) {
		world = _world;
		ens = new LinkedList<Enemy>();
		ens = world.ens;
		map = new boolean[64][64];
		for (Tile tile : world.getTiles())
			if (tile.getType() == TileType.WALL)
				map[(int) (tile.getPos().x / 64)][(int) (tile.getPos().y / 64)] = true;

		astar = new Astar(64, 64) {
			protected boolean isValid(int x, int y) {
				return !map[x][y];
			}
		};
	}

	public void update(float delta) {
		Iterator<Enemy> iter = ens.iterator();
		while (iter.hasNext()) {
			Enemy e = iter.next();
			if(!e.Alive())
				continue;
			if((e.targetx / 64 < 64 && e.targetx / 64 > 0 && e.targety / 64 > 0 && e.targety / 64 < 64) && astar.isValid(e.targetx / 64, e.targety / 64) )
				e.setPath(astar.getPath(e.startx / 64, e.starty / 64, e.targetx / 64, e.targety / 64));
			if (e.canShoot)
				world.addBullet(e.fire());
			e.update(delta);
		}
	}
	
	public static boolean collisionsEnemy(Rectangle _box, Enemy act) {
		for (Enemy e : ens) {
			if(e.Alive() && e != act && e.getBoundingBox().intersects(_box))
				return true;
		}
		return false;
	}

	public LinkedList<Enemy> getEnemies() {
		return ens;
	}

	public void add(Enemy e) {
		ens.add(e);
	}

	public LinkedList<Enemy> getList() {
		return ens;
	}

}
