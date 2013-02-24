package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.blocks.BaseItemStack;

import cpw.mods.fml.server.FMLServerHandler;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class ForgeUtil {

    public static boolean hasPermission(EntityPlayerMP player, String perm) {
        return FMLServerHandler.instance().getServer().getConfigurationManager().getOps().contains(player.username);
    }

    public static ItemStack toForgeItemStack(BaseItemStack item) {
        ItemStack ret = new ItemStack(item.getType(), item.getAmount(), item.getData());
        for (Map.Entry entry : item.getEnchantments().entrySet()) {
            ret.addEnchantment(net.minecraft.enchantment.Enchantment.enchantmentsList[((Integer)entry.getKey()).intValue()], ((Integer)entry.getValue()).intValue());
        }

        return ret;
    }
}