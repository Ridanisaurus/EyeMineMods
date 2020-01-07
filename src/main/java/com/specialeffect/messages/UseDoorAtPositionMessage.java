/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.messages;

import javax.xml.ws.handler.MessageContext;

import com.specialeffect.utils.OpenableBlock;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class UseDoorAtPositionMessage implements IMessage {
    
    private BlockPos blockPos;
    private boolean toBeOpened;

    public UseDoorAtPositionMessage() { }

    public UseDoorAtPositionMessage(BlockPos pos, boolean toOpen) {
        this.blockPos = pos;
        this.toBeOpened= toOpen;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	toBeOpened = ByteBufUtils.readVarInt(buf, 1) > 0;
        int x = ByteBufUtils.readVarInt(buf, 5); 
        int y = ByteBufUtils.readVarInt(buf, 5); 
        int z = ByteBufUtils.readVarInt(buf, 5); 
        blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeVarInt(buf, toBeOpened ? 1 : 0, 1);
        ByteBufUtils.writeVarInt(buf, blockPos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getZ(), 5);       
    }

    public static class Handler implements IMessageHandler<UseDoorAtPositionMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final UseDoorAtPositionMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getInstance() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    PlayerEntity player = ctx.getServerHandler().playerEntity;
                    World world = player.getEntityWorld();
					Block block = world.getBlockState(message.blockPos).getBlock();
					if (message.toBeOpened) {
						OpenableBlock.open(world, block, message.blockPos);
					}
					else {
						OpenableBlock.close(world, block, message.blockPos);
					}
                }
            });
            return null; // no response in this case
        }
    }
}
