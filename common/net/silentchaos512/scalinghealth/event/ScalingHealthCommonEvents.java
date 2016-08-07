package net.silentchaos512.scalinghealth.event;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ScalingHealthCommonEvents {

  @SubscribeEvent
  public void onLivingDrops(LivingDropsEvent event) {

    // Handle heart drops.
    EntityLivingBase entityLiving = event.getEntityLiving();
    if (!entityLiving.worldObj.isRemote) {
      Random rand = ScalingHealth.random;
      int stackSize = 0;
      if (event.isRecentlyHit() && rand.nextFloat() <= ConfigScalingHealth.HEART_DROP_CHANCE) {
        stackSize += 1;
      }

      if (!entityLiving.isNonBoss()) {
        int min = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MIN;
        int max = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MAX;
        stackSize += min + rand.nextInt(max - min + 1);
      }

      if (stackSize > 0)
        entityLiving.dropItem(ModItems.heart, stackSize);
    }
  }

  @SubscribeEvent
  public void onPlayerRespawn(
      net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {

    // Set player health correctly after respawn.
    if (event.player instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.player;
      PlayerData data = SHPlayerDataHandler.get(player);

      // Lose health on death?
      if (ConfigScalingHealth.LOSE_HEALTH_ON_DEATH) {
        data.setMaxHealth(ConfigScalingHealth.PLAYER_STARTING_HEALTH);
      }

      // Apply health modifier
      float maxHealth = data.getMaxHealth();
      ModifierHandler.setMaxHealth(player, maxHealth, 0);
      if (player.getHealth() != maxHealth)
        player.setHealth(maxHealth);
    }
  }

  @SubscribeEvent
  public void onPlayerJoinedServerEvent(PlayerLoggedInEvent event) {

    // Sync player data and set health.
    if (event.player instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.player;
      PlayerData data = SHPlayerDataHandler.get(player);

      // Apply health modifier
      float maxHealth = data.getMaxHealth();
      ModifierHandler.setMaxHealth(player, maxHealth, 0);
      if (player.getHealth() > maxHealth)
        player.setHealth(maxHealth);
    }
  }

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {

    if (event.getModID().equals(ScalingHealth.MOD_ID_LOWER)) {
      ConfigScalingHealth.load();
      ConfigScalingHealth.save();
    }
  }
}
