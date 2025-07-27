package net.szum123321.ariadne_glasses.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.szum123321.ariadne_glasses.AriadneGlasses;

public record AriadneResetPacket() implements CustomPayload {
	public final static CustomPayload.Id<AriadneResetPacket> ID = new CustomPayload.Id<>(AriadneGlasses.ARIADNE_RESET_PACKET);
	public static final PacketCodec<RegistryByteBuf, AriadneResetPacket> CODEC = PacketCodec.unit(new AriadneResetPacket());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
