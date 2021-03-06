package it.unical.igpe.logic;

import java.awt.Rectangle;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

import it.unical.igpe.MapUtils.World;
import it.unical.igpe.ai.EnemyManager;
import it.unical.igpe.utils.GameConfig;
import it.unical.igpe.utils.TileType;
import it.unical.igpe.utils.Updatable;

public class Enemy extends AbstractDynamicObject implements Updatable {
	public boolean chaseObj;
	public boolean canShoot;
	public boolean canMove;
	public boolean isMoving;
	public float timeToNextStep;
	public int startx;
	public int starty;
	public int targetx;
	public int targety;
	private float shootDelay;
	private float followDelay;
	private float followTimer;
	private TileType nextTile;
	private Rectangle box;
	private Vector2 dir;
	private IntArray path;

	public Enemy(Vector2 _pos) {
		boundingBox = new Rectangle((int) _pos.x, (int) _pos.y, 64, 64);
		ID = "enemy";
		alive = true;
		HP = 100f;
		speed = 1.5f;
		chaseObj = true;
		canShoot = false;
		path = new IntArray();
		dir = new Vector2();
		Random random = new Random();
		followTimer = random.nextFloat() + 6f;
		startx = this.getX() + 32;
		starty = this.getY() + 32;
		targetx = startx;
		targety = starty;
		canMove = true;
	}

	public void update(float delta) {
		canMove = true;
		isMoving = false;
		startx = this.getX() + 32;
		starty = this.getY() + 32;
		if (this.getPos().dst(World.player.getPos()) < GameConfig.ENEMY_RADIUS
				&& this.getPos().dst(World.player.getPos()) > GameConfig.ENEMY_SHOOT_RADIUS) {
			targetx = World.player.getX() + 32;
			targety = World.player.getY() + 32;
			followDelay = 0;
		} else if (followDelay > followTimer) {
			Random r = new Random();
			targetx = startx + (r.nextInt(16) - 8) * 32;
			targety = starty + (r.nextInt(16) - 8) * 32;
			followDelay = 0;
		} else if (this.getPos().dst(World.player.getPos()) < GameConfig.ENEMY_SHOOT_RADIUS) {
			canMove = false;
			followDelay = 0;
			targetx = World.player.getX() + 32;
			targety = World.player.getY() + 32;
		}

		dir = new Vector2(targetx - startx, targety - starty);
		dir.rotate90(-1);
		angle = dir.angle();

		if (this.getPos().dst(World.player.getPos()) <= GameConfig.ENEMY_SHOOT_RADIUS) {
			targetx = World.player.getX() + 32;
			targety = World.player.getY() + 32;
			followDelay = 0;
			if (shootDelay > 1) {
				shootDelay = 0;
				canShoot = true;
			}
		}
		if (path.size != 0 && canMove) {
			isMoving = true;
			float y = path.pop();
			float x = path.pop();
			this.followPath(new Vector2(x * 64, y * 64), delta);
		}

		shootDelay += delta;
		followDelay += delta;
	}

	public Bullet fire() {
		this.canShoot = false;
		return new Bullet(new Vector2(this.getX() + 32, this.getY() + 32),
				(float) Math.toRadians(angle + 90f), "enemy", 15);
	}

	public void hit(float dmg) {
		this.HP -= dmg;
		if (HP <= 0)
			alive = false;
	}

	public void setPath(IntArray intArray) {
		path = intArray;
	}

	public IntArray getPath() {
		return path;
	}

	public void followPath(Vector2 pos, float delta) {
		if (this.getY() > pos.y) {
			box = new Rectangle(this.getX(), this.getY() - (int) (GameConfig.ENEMY_SPEED * delta),
					this.getBoundingBox().width, this.getBoundingBox().height);
			nextTile = World.getNextTile(box);
			if (nextTile != TileType.WALL && !EnemyManager.collisionsEnemy(box, this))
				this.getBoundingBox().y -= GameConfig.ENEMY_SPEED * delta;
		}
		if (this.getX() > pos.x) {
			box = new Rectangle(this.getX() - (int) (GameConfig.ENEMY_SPEED * delta), this.getY(),
					this.getBoundingBox().width, this.getBoundingBox().height);
			nextTile = World.getNextTile(box);
			if (nextTile != TileType.WALL && !EnemyManager.collisionsEnemy(box, this))
				this.getBoundingBox().x -= GameConfig.ENEMY_SPEED * delta;
		}
		if (this.getY() < pos.y) {
			box = new Rectangle(this.getX(), this.getY() + (int) (GameConfig.ENEMY_SPEED * delta),
					this.getBoundingBox().width, this.getBoundingBox().height);
			nextTile = World.getNextTile(box);
			if (nextTile != TileType.WALL && !EnemyManager.collisionsEnemy(box, this))
				this.getBoundingBox().y += GameConfig.ENEMY_SPEED * delta;
		}
		if (this.getX() < pos.x) {
			box = new Rectangle(this.getX() + (int) (GameConfig.ENEMY_SPEED * delta), this.getY(),
					this.getBoundingBox().width, this.getBoundingBox().height);
			nextTile = World.getNextTile(box);
			if (nextTile != TileType.WALL && !EnemyManager.collisionsEnemy(box, this))
				this.getBoundingBox().x += GameConfig.ENEMY_SPEED * delta;
		}
	}
	
}
