package it.unical.igpe.net;

import java.net.InetAddress;

import com.badlogic.gdx.math.Vector2;

import it.unical.igpe.game.IGPEGame;
import it.unical.igpe.logic.Player;
import it.unical.igpe.net.packet.Packet03Fire;

public class PlayerMP extends Player {

	public MultiplayerWorld world;
	public InetAddress ipAddress;
	public int port;

	public PlayerMP(Vector2 _pos, MultiplayerWorld _world, String username, InetAddress ipAddress, int port) {
		super(_pos, null, username);
		this.world = _world;
		this.activeWeapon = pistol;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public void fire() {
		Packet03Fire packetFire;
		if (!reloading) {
			if(this.activeWeapon.ID == "pistol")
				packetFire = new Packet03Fire(username, boundingBox.x, boundingBox.y, angle, 1);
			else if(this.activeWeapon.ID == "shotgun")
				packetFire = new Packet03Fire(username, boundingBox.x, boundingBox.y, angle, 2);
			else 
				packetFire = new Packet03Fire(username, boundingBox.x, boundingBox.y, angle, 3);
			
			packetFire.writeData(IGPEGame.game.socketClient);
			this.activeWeapon.lastFired = 0f;
			this.activeWeapon.actClip--;
		}
	}

}
