package com.mguhc.listener;

import com.mguhc.Blb;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ScenariosListener implements Listener {

    @EventHandler
    private void OnPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        // Vérifier si le bloc placé est de l'eau ou de la lave
        if (block.getType().equals(Material.WATER) || block.getType().equals(Material.LAVA)) {
            // Remplacer le bloc par un bloc statique immédiatement
            block.setType(block.getType()); // Remplace le bloc par le même type
        }
        else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    block.setType(Material.AIR);
                }
            }.runTaskLater(Blb.getInstance(), 5*20);
        }
    }
}
