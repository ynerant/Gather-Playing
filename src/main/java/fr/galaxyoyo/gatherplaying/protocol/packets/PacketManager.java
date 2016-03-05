package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.Player;
import io.netty.channel.ChannelFutureListener;

import java.util.Map;

public class PacketManager
{
	private static final Map<Byte, Class<? extends Packet>> packets = Maps.newHashMapWithExpectedSize(64);
	private static final Map<Class<? extends Packet>, Byte> packetIds = Maps.newHashMapWithExpectedSize(64);

	public static void registerPacket(int discriminator, Class<? extends Packet> pktClazz)
	{
		if (packets.containsKey((byte) discriminator))
			throw new IllegalArgumentException("A packet with id '" + discriminator + "' already exists!'");
		if (packets.containsValue(pktClazz))
			throw new IllegalArgumentException("The packet '" + pktClazz.getSimpleName() + "' already exists!");
		if (discriminator >= 64)
			throw new IllegalArgumentException("Can't create packet with id >= 64!");
		if (discriminator < 0)
			throw new IllegalArgumentException("Can't create packet with id < 0!");
		if (pktClazz.isInterface())
			throw new IllegalArgumentException("Class '" + pktClazz.getSimpleName() + "' isn't defined!");
		packets.put((byte) discriminator, pktClazz);
		packetIds.put(pktClazz, (byte) discriminator);
	}

	protected static byte getPacketId(Packet pkt) { return packetIds.get(pkt.getClass()); }

	public static <T extends Packet> T createPacket(Class<T> pktClazz)
	{
		try
		{
			return pktClazz.newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public static Packet createPacket(int packetId) { return createPacket(packets.get((byte) packetId)); }

	public static void sendPacketToPlayer(Player player, Packet pkt)
	{
		if (Utils.getSide() == Side.CLIENT)
			throw new UnsupportedOperationException("Can't send a packet player->player !");
		pkt.player = player;
		if (player.connection.eventLoop().inEventLoop())
			player.connection.writeAndFlush(pkt).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		else
			player.connection.eventLoop().execute(() -> player.connection.writeAndFlush(pkt).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE));
	}

	public static void sendPacketToServer(Packet pkt)
	{
		if (Utils.getSide() == Side.SERVER)
			throw new UnsupportedOperationException("Can't send a packet server->server !");
		if (Client.localPlayer.connection.eventLoop().inEventLoop())
			Client.localPlayer.connection.writeAndFlush(pkt).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		else
			Client.localPlayer.connection.eventLoop().execute(
					() -> Client.localPlayer.connection.writeAndFlush(pkt).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE));
	}

	public static void sendPacketToParty(Party party, Packet pkt)
	{
		for (Player player : party.getOnlinePlayers())
			sendPacketToPlayer(player, pkt);
	}

	static
	{
		registerPacket(0x00, PacketInConnect.class);
		registerPacket(0x01, PacketOutConnectResponse.class);
		registerPacket(0x02, PacketOutCosts.class);
		registerPacket(0x03, PacketMixDeck.class);
		registerPacket(0x04, PacketMixUpdatePartyInfos.class);
		registerPacket(0x05, PacketMixChat.class);
		registerPacket(0x06, PacketInSelectDeck.class);
		registerPacket(0x07, PacketOutPartyStart.class);
		registerPacket(0x08, PacketMixEditLife.class);
		registerPacket(0x09, PacketMixDrawCard.class);
		registerPacket(0x0A, PacketMixPlayCard.class);
		registerPacket(0x0B, PacketMixTapCard.class);
		registerPacket(0x0C, PacketMixDestroyCard.class);
		registerPacket(0x0D, PacketMixSetPhase.class);
		registerPacket(0x0E, PacketInShuffle.class);
		registerPacket(0x0F, PacketMixPlayDestroyed.class);
		registerPacket(0x10, PacketMixGainControl.class);
		registerPacket(0x11, PacketMixAddMarker.class);
		registerPacket(0x12, PacketMixRemoveMarker.class);
		registerPacket(0x13, PacketMixSetLife.class);
		registerPacket(0x14, PacketMixFindCard.class);
		registerPacket(0x15, PacketMixPlayFounded.class);
		registerPacket(0x16, PacketMixScry.class);
		registerPacket(0x17, PacketMixSetCardType.class);
		registerPacket(0x18, PacketMixSetCardSubtypes.class);
		registerPacket(0x19, PacketMixInvokeToken.class);
		registerPacket(0x1A, PacketMixMoveCard.class);
		registerPacket(0x1B, PacketMixReturnCard.class);
		registerPacket(0x1C, PacketMixDrawLine.class);
	}
}