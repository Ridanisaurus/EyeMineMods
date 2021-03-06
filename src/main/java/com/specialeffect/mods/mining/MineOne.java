/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.AirBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


public class MineOne 
 extends ChildMod
{
	public final String MODID = "autodestroy";

	private boolean mDestroying = false;
	private BlockPos mBlockToDestroy;
	private static KeyBinding mDestroyKB;
	
	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings	
		mDestroyKB = new KeyBinding("Mine one block", GLFW.GLFW_KEY_N, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mDestroyKB);
				
	}
		
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			if (mDestroying) {
				// Select the best tool from the inventory
				World world = Minecraft.getInstance().world;
	    		
    			// Swords can't destroy blocks: warn user
    			if (player.getHeldItemMainhand().getItem() instanceof SwordItem) {
    				String message = "Can't destroy blocks with a sword, please select another item";
			        player.sendMessage(new StringTextComponent(message));
			        
    				this.stopDestroying();
    				return;
    			}
    		
				
				// Stop attacking if we're not pointing at the block any more
				// (which means either we've destroyed it, or moved away)
				RayTraceResult mov = Minecraft.getInstance().objectMouseOver;
				boolean blockDestroyed = (world.getBlockState(mBlockToDestroy).getBlock() instanceof AirBlock);
				boolean movedAway =  false;		
				BlockPos pos = this.getMouseOverBlockPos();
				if (pos != null) {														
					movedAway = mBlockToDestroy.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), false) > 0.5;					
				}
				
				if (mov == null || blockDestroyed || movedAway) {					
					this.stopDestroying();
				}
			}
		}
	}
	
	private void startDestroying() {
		mDestroying = true;
		
		final KeyBinding attackBinding = 
				Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKey(), true);	

	}
	
	private void stopDestroying() {
		final KeyBinding attackBinding = 
				Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKey(), false);
		mDestroying = false;
	}
	
	// Return the position of the block that the mouse is pointing at.
	// May be null, if pointing at something other than a block.
	private BlockPos getMouseOverBlockPos() {
		BlockPos pos = null;			
		BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock();
		if (rayTraceBlock != null) {			
			pos = rayTraceBlock.getPos();			
		}
		return pos;
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {   
		
		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		if(mDestroyKB.getKey().getKeyCode() == event.getKey()) {
			// turn off continuous mining
			ContinuouslyMine.stop();
			
			// mine the block you're facing
			mBlockToDestroy = this.getMouseOverBlockPos();
			if (mBlockToDestroy == null) {
				LOGGER.debug("Nothing to attack");
				return;
			}
			else {
				this.startDestroying();
			}
		}
	}
	
}
