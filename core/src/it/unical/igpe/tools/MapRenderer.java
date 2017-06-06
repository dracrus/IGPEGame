package it.unical.igpe.tools;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import it.unical.igpe.game.World;
import it.unical.igpe.logic.Bullet;
import it.unical.igpe.logic.Enemy;
import it.unical.igpe.logic.Player;
import it.unical.igpe.logic.Tile;

public class MapRenderer {
	World world;
	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer sr;
	TextureRegion currentFrame;
	FrameBuffer fbo;
	

	float stateTime;
	Player player;
	LinkedList<Bullet> bls;

	public MapRenderer(World _world) {
		this.world = _world;
		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(true, 800, 800);
		this.camera.position.set(world.getPlayer().getBoundingBox().x, world.getPlayer().getBoundingBox().y, 0);
		this.batch = new SpriteBatch();
		this.batch.setColor(1, 1, 1, 0.5f);
		fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		this.sr = new ShapeRenderer();

		this.player = world.getPlayer();
		this.bls = world.getPlayer().getBullets();
	}

	public void render(float deltaTime) {
		stateTime += deltaTime;
		batch.setProjectionMatrix(camera.combined);
		sr.setProjectionMatrix(camera.combined);

		camera.position.x = world.getPlayer().getBoundingBox().x;
		camera.position.y = world.getPlayer().getBoundingBox().y;
		camera.update();

		// Rendering different weapons
		if (world.getPlayer().getActWeapon() == "pistol")
			switch (world.state) {
			case IDLE:
				currentFrame = Assets.idlePistolAnimation.getKeyFrame(stateTime, true);
				break;
			case RUNNING:
				currentFrame = Assets.runningPistolAnimation.getKeyFrame(stateTime, true);
				break;
			case RELOADING:
				currentFrame = Assets.reloadingPistolAnimation.getKeyFrame(stateTime, true);
				break;
			}
		else if (world.getPlayer().getActWeapon() == "shotgun")
			switch (world.state) {
			case IDLE:
				currentFrame = Assets.idleShotgunAnimation.getKeyFrame(stateTime, true);
				break;
			case RUNNING:
				currentFrame = Assets.runningShotgunAnimation.getKeyFrame(stateTime, true);
				break;
			case RELOADING:
				currentFrame = Assets.reloadingShotgunAnimation.getKeyFrame(stateTime, true);
				break;
			}
		else if (world.getPlayer().getActWeapon() == "rifle")
			switch (world.state) {
			case IDLE:
				currentFrame = Assets.idleRifleAnimation.getKeyFrame(stateTime, true);
				break;
			case RUNNING:
				currentFrame = Assets.runningRifleAnimation.getKeyFrame(stateTime, true);
				break;
			case RELOADING:
				currentFrame = Assets.reloadingRifleAnimation.getKeyFrame(stateTime, true);
				break;
			}

		bls = player.getBullets();

		// draw map
		
		fbo.begin();
		batch.begin();
		for (Enemy e : world.EM.getList()) {
			batch.draw(Assets.Light, e.getPos().x - 50,e.getPos().y - 50, 200, 200);
		}
		batch.end();
		fbo.end();
		
		batch.begin();
		for (Tile tile : world.getTiles()) {
			if (tile.getType() == TileType.GROUND)
				batch.draw(Assets.Ground, tile.getBoundingBox().x, tile.getBoundingBox().y);
			else if (tile.getType() == TileType.WALL)
				batch.draw(Assets.Wall, tile.getBoundingBox().x, tile.getBoundingBox().y);
			else if (tile.getType() == TileType.ENDLEVEL)
				batch.draw(Assets.Stair, tile.getBoundingBox().x, tile.getBoundingBox().y);
		}
		batch.draw(currentFrame, world.getPlayer().getBoundingBox().x , world.getPlayer().getBoundingBox().y , 32, 32, 64,
				64, 1f, 1f, player.angle);
		for (Enemy e : world.EM.getList()) {
			batch.draw(Assets.Enemy, e.getPos().x, e.getPos().y, 32, 32, 64, 64, 1f, 1f, e.angle);
		}
		batch.end();
		

		sr.begin(ShapeType.Filled);
		sr.setColor(Color.RED);
		for (Bullet bullet : bls) {
			sr.rect(bullet.getPos().x, bullet.getPos().y, bullet.getBoundingBox().width,
					bullet.getBoundingBox().height);
		}
		for (Enemy e : world.EM.getEnemies()) {
			for(int i = 0; i < e.getPath().size; i+= 2) {
				sr.circle(e.getPath().get(i) * 64, e.getPath().get(i + 1) * 64, 2);
			}
		}
		sr.end();

		sr.begin(ShapeType.Line);
		sr.setColor(Color.BLACK);
		for (Enemy e : world.EM.getList()) {
			sr.circle(e.getPos().x + 32, e.getPos().y + 32, 256);
			sr.circle(e.getPos().x + 32, e.getPos().y + 32, 192);
		}
		
		sr.end();
	}

	public void dispose() {
		batch.dispose();
		sr.dispose();
	}
}
