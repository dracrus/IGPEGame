package it.unical.igpe.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import it.unical.igpe.game.IGPEGame;
import it.unical.igpe.net.packet.Packet;
import it.unical.igpe.net.packet.Packet.PacketTypes;
import it.unical.igpe.net.packet.Packet00Login;
import it.unical.igpe.net.packet.Packet01Disconnect;
import it.unical.igpe.net.packet.Packet02Move;
import it.unical.igpe.net.packet.Packet03Fire;
import it.unical.igpe.net.packet.Packet04Death;
import it.unical.igpe.net.packet.Packet05GameOver;

public class GameServer extends Thread {
	public MultiplayerWorld worldMP;
	public int MaxKills;
	private DatagramSocket socket;
	private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();

	public GameServer(int port) {
		try {
			this.socket = new DatagramSocket(port);
			System.out.println("Creating Server...");
			this.worldMP = new MultiplayerWorld("Arena.map", true);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			for (PlayerMP p : connectedPlayers) {
				if(p.kills >= MaxKills)  {
					Packet05GameOver packetGO = new Packet05GameOver(p.username, p.kills);
					packetGO.writeData(this);
				}
			}
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "]"
					+ ((Packet00Login) packet).getUsername() + " has connected");
			PlayerMP player = new PlayerMP(
					new Vector2(((Packet00Login) packet).getX(), ((Packet00Login) packet).getY()),
					IGPEGame.game.worldMP, ((Packet00Login) packet).getUsername(), address, port);
			this.addConnection(player, (Packet00Login) packet);
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "] "
					+ ((Packet01Disconnect) packet).getUsername() + " has left...");
			this.removeConnection((Packet01Disconnect) packet);
			break;
		case MOVE:
			packet = new Packet02Move(data);
			this.handleMove((Packet02Move) packet);
			break;
		case FIRE:
			packet = new Packet03Fire(data);
			handleFire((Packet03Fire) packet);
			break;
		case DEATH:
			packet = new Packet04Death(data);
			handleDeath((Packet04Death) packet);
			break;
		case GAMEOVER:
			packet = new Packet05GameOver(data);
			packet.writeData(this);
			break;
		}
	}
	
	private void handleDeath(Packet04Death packet) {
		if (getPlayerMP(packet.getUsernameKiller()) != null && getPlayerMP(packet.getUsernameKilled()) != null) {
			int index = getPlayerMPIndex(packet.getUsernameKiller());
			if(!packet.getUsernameKiller().equalsIgnoreCase(MultiplayerWorld.username))
				this.connectedPlayers.get(index).kills++;
			index = getPlayerMPIndex(packet.getUsernameKilled());
			if(!packet.getUsernameKilled().equalsIgnoreCase(MultiplayerWorld.username))
				this.connectedPlayers.get(index).deaths++;
			packet.writeData(this);
		}
	}

	public void close() {
		this.socket.close();
	}

	private void handleFire(Packet03Fire packet) {
		if (getPlayerMP(packet.getUsername()) != null) {
			int index = getPlayerMPIndex(packet.getUsername());
			this.connectedPlayers.get(index).getBoundingBox().x = packet.getX();
			this.connectedPlayers.get(index).getBoundingBox().y = packet.getY();
			this.connectedPlayers.get(index).angle = packet.getAngle();
			packet.writeData(this);
		}
	}

	private void removeConnection(Packet01Disconnect packet) {
		this.connectedPlayers.remove(getPlayerMPIndex(packet.getUsername()));
		packet.writeData(this);
	}

	private void handleMove(Packet02Move packet) {
		if (getPlayerMP(packet.getUsername()) != null) {
			int index = getPlayerMPIndex(packet.getUsername());
			PlayerMP plMP = this.connectedPlayers.get(index);
			plMP.getBoundingBox().x = packet.getX();
			plMP.getBoundingBox().y = packet.getY();
			plMP.angle = packet.getAngle();
			plMP.state = packet.getState();
			packet.writeData(this);
		}
	}

	public void addConnection(PlayerMP player, Packet00Login packet) {
		boolean alreadyConnected = false;
		for (PlayerMP p : this.connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				if (p.ipAddress == null)
					p.ipAddress = player.ipAddress;
				if (p.port == -1)
					p.port = player.port;
				alreadyConnected = true;
			}
			sendData(packet.getData(), p.ipAddress, p.port);

			Packet newPacket = new Packet00Login(p.getUsername(), p.getBoundingBox().x, p.getBoundingBox().y);
			sendData(newPacket.getData(), player.ipAddress, player.port);
		}
		if (!alreadyConnected) {
			this.connectedPlayers.add(player);
		}

	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDataToAllClients(byte[] data) {
		for (PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

	public PlayerMP getPlayerMP(String username) {
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(username)) {
				return player;
			}
		}
		return null;
	}

	public int getPlayerMPIndex(String username) {
		int index = 0;
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(username)) {
				break;
			}
			index++;
		}
		return index;
	}
}
