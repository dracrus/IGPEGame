package it.unical.igpe.GUI.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import it.unical.igpe.GUI.Assets;
import it.unical.igpe.GUI.SoundManager;
import it.unical.igpe.game.IGPEGame;

public class LoadingScreen implements Screen {
	public static boolean isMP = false;
	private SpriteBatch batch;
	public Stage stage;
	private Table table;
	private ProgressBar loadingBar;
	private Label loading;

	@Override
	public void show() {
		Assets.load();
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 900, 506);

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		loading = new Label("LOADING...", IGPEGame.skinui);

		loadingBar = new ProgressBar(0.0f, 1.0f, 0.1f, false, IGPEGame.skinui);
		loadingBar.setValue(0);
		table.add(loading);
		table.row();
		table.add(loadingBar);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(IGPEGame.background, 0, 0);
		batch.end();

		loadingBar.setValue(Assets.manager.getProgress());

		if (Assets.manager.update() && SoundManager.manager.update()) {
			Assets.manager.finishLoading();
			if (!isMP) 
				IGPEGame.game.setScreen(ScreenManager.GS);
			else {
				isMP = false;
				IGPEGame.game.setScreen(ScreenManager.MGS);
			}
		}

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		this.show();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		stage.dispose();
		batch.dispose();
	}

}
