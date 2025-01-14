package com.teamresourceful.resourcefulbees.common.network.packets;

import com.teamresourceful.resourcefulbees.common.tileentity.multiblocks.apiary.ApiaryTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateClientApiaryMessage {
    private final BlockPos pos;
    private final CompoundNBT data;

    public UpdateClientApiaryMessage(BlockPos pos, CompoundNBT data){
        this.pos = pos;
        this.data = data;
    }

    public static void encode(UpdateClientApiaryMessage message, PacketBuffer buffer){
        buffer.writeBlockPos(message.pos);
        buffer.writeNbt(message.data);
    }

    public static UpdateClientApiaryMessage decode(PacketBuffer buffer){
        return new UpdateClientApiaryMessage(buffer.readBlockPos(), buffer.readNbt());
    }

    public static void handle(UpdateClientApiaryMessage message, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null && player.level.isLoaded(message.pos)) {
                TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                if (tileEntity instanceof ApiaryTileEntity) {
                    ApiaryTileEntity apiaryTileEntity = (ApiaryTileEntity) tileEntity;
                    apiaryTileEntity.bees.clear();
                    apiaryTileEntity.loadFromNBT(message.data);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}


