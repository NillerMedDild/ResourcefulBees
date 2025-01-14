package com.teamresourceful.resourcefulbees.common.network.packets;

import com.teamresourceful.resourcefulbees.common.lib.enums.ApiaryTab;
import com.teamresourceful.resourcefulbees.common.tileentity.multiblocks.apiary.IApiaryMultiblock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ApiaryTabMessage {

    private final BlockPos pos;
        private final ApiaryTab tab;

        public ApiaryTabMessage(BlockPos pos, ApiaryTab tab){
            this.pos = pos;
            this.tab = tab;
        }

        public static void encode(ApiaryTabMessage message, PacketBuffer buffer){
            buffer.writeBlockPos(message.pos);
            buffer.writeEnum(message.tab);
        }

        public static ApiaryTabMessage decode(PacketBuffer buffer){
            return new ApiaryTabMessage(buffer.readBlockPos(), buffer.readEnum(ApiaryTab.class));
        }

        public static void handle(ApiaryTabMessage message, Supplier<NetworkEvent.Context> context){
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                if (player != null && player.level.isLoaded(message.pos)) {
                    TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                    if (tileEntity instanceof IApiaryMultiblock) {
                        ((IApiaryMultiblock) tileEntity).switchTab(player, message.tab);
                    }
                }
            });
            context.get().setPacketHandled(true);
        }
    }




