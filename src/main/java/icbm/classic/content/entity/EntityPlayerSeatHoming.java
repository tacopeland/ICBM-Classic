package icbm.classic.content.entity;

import com.builtbroken.mc.imp.transform.vector.Pos;

import icbm.classic.ICBMClassic;
import icbm.classic.content.items.ItemTracker;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 * Used a placeholder to move riding entities around
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/31/2017.
 */
public class EntityPlayerSeatHoming extends EntityPlayerSeat
{
    public EntityPlayerSeatHoming(World world)
    {
        super(world);
    }

    @Override
    public boolean interactFirst(EntityPlayer entityPlayer)
    {
    	ItemStack itemStack = entityPlayer.getCurrentEquippedItem();
    	if (!this.worldObj.isRemote && itemStack != null)
        {
            if (itemStack.getItem() instanceof ItemTracker)
            {
                Entity trackingEntity = ((ItemTracker) itemStack.getItem()).getTrackingEntity(this.worldObj, itemStack);

                if (trackingEntity != null)
                {
                	if (trackingEntity instanceof EntityPlayer) {
                    	ICBMClassic.INSTANCE.logger().info(entityPlayer.getDisplayName() + " is tracking " + ((EntityPlayer) trackingEntity).getDisplayName());
                	}
                	
                	entityPlayer.addChatMessage(new ChatComponentText("Missile target locked to: " + trackingEntity.getCommandSenderName()));
                	TileLauncherScreen controller = ((TileLauncherBase) this.host).launchScreen;
                	
                    Pos newTarget = new Pos(trackingEntity.posX, 0, trackingEntity.posZ);
                    controller.setTarget(newTarget);
                    
                    return true;
                }
            }
        }
    	
        //Handle player riding missile
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != entityPlayer)
        {
            return true;
        }
        else if (this.riddenByEntity != null && this.riddenByEntity != entityPlayer)
        {
            return false;
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                entityPlayer.mountEntity(this);
            }
            return true;
        }
    }
}
