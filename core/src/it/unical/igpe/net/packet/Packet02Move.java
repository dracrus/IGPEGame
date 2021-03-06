package it.unical.igpe.net.packet;

import it.unical.igpe.net.GameClient;
import it.unical.igpe.net.GameServer;

public class Packet02Move extends Packet {

	private String username;
	private int x, y, state, weapon;
	private float angle;

	public Packet02Move(byte[] data) {
		super(02);
		String[] dataArray = readData(data).split(",");
		this.username = dataArray[0];
		this.x = Integer.parseInt(dataArray[1]);
		this.y = Integer.parseInt(dataArray[2]);
		this.angle = Float.parseFloat(dataArray[3]);
		this.state = Integer.parseInt(dataArray[4]);
		this.weapon = Integer.parseInt(dataArray[5]);
	}

	public Packet02Move(String username, int x, int y, float angle, int state, int weapon) {
		super(02);
		this.username = username;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.state = state;
		this.weapon = weapon;
	}

	@Override
	public void writeData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("02" + this.username + "," + this.x + "," + this.y + "," + this.angle + "," + this.state + ","
				+ this.weapon).getBytes();
	}

	public String getUsername() {
		return username;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public float getAngle() {
		return this.angle;
	}

	public int getState() {
		return this.state;
	}
	
	public int getWeapon() {
		return this.weapon;
	}

}
