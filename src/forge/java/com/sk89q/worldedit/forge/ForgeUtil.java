package com.sk89q.worldedit.forge;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.sk89q.worldedit.blocks.BaseItemStack;

import cpw.mods.fml.common.FMLCommonHandler;

public class ForgeUtil {

    public static boolean hasPermission(EntityPlayerMP player, String perm) {
        // TODO fix WEPIF
        return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().areCommandsAllowed(player.username);
    }

    public static ItemStack toForgeItemStack(BaseItemStack item) {
        ItemStack ret = new ItemStack(item.getType(), item.getAmount(), item.getData());
        for (Map.Entry entry : item.getEnchantments().entrySet()) {
            ret.addEnchantment(net.minecraft.enchantment.Enchantment.enchantmentsList[((Integer)entry.getKey()).intValue()], ((Integer)entry.getValue()).intValue());
        }

        return ret;
    }
}