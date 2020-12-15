package icbm.classic.content.items;

import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class ItemTracker extends ItemICBMElectrical implements IRecipeContainer
{
    private static final long ENERGY_PER_TICK = 1;

    public ItemTracker()
    {
        super("tracker");
        this.setMaxStackSize(1);
        //FlagRegistry.registerFlag("ban_Tracker");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        if (par1IconRegister instanceof TextureMap)
        {
            ((TextureMap) par1IconRegister).setTextureEntry(this.getUnlocalizedName().replace("item.", ""), new TextureTracker());
            this.itemIcon = ((TextureMap) par1IconRegister).getTextureExtry(this.getUnlocalizedName().replace("item.", ""));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List lines)
    {
        Entity trackingEntity = getTrackingEntity(FMLClientHandler.instance().getClient().theWorld, stack);

        if (trackingEntity != null)
        {
            lines.add(LanguageUtility.getLocal("info.tracker.tracking") + " " + trackingEntity.getCommandSenderName());
        }

        lines.add(LanguageUtility.getLocal("info.tracker.tooltip"));

        if (player.getCommandSenderName().equalsIgnoreCase("Biffa2001"))
        {
            lines.add("");
            lines.add("psst use me biffa!!");
        }
    }


    public void setTrackingEntity(ItemStack itemStack, Entity entity)
    {
        if (itemStack.stackTagCompound == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        if (entity != null)
        {
            itemStack.stackTagCompound.setInteger("trackingEntity", entity.getEntityId());
        }
        
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getEntityWorld().getWorldInfo().getWorldName().contains("spawn")) {
            EntityPlayer p = (EntityPlayer) entity;
            ChatComponentText msg = new ChatComponentText("!!! WARNING !!! YOU ARE BEING TRACKED");
            msg.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
            p.addChatMessage(msg);
        }
    }


    public Entity getTrackingEntity(World worldObj, ItemStack itemStack)
    {
        if (worldObj != null)
        {
            if (itemStack.stackTagCompound != null)
            {
                int trackingID = itemStack.stackTagCompound.getInteger("trackingEntity");
                return worldObj.getEntityByID(trackingID);
            }
        }
        return null;
    }

    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        super.onCreated(par1ItemStack, par2World, par3EntityPlayer);
        setTrackingEntity(par1ItemStack, par3EntityPlayer);
    }

    @Override
    public void onUpdate(ItemStack itemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        super.onUpdate(itemStack, par2World, par3Entity, par4, par5);

        if (par3Entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) par3Entity;

            if (player.inventory.getCurrentItem() != null)
            {
                if (player.inventory.getCurrentItem().getItem() == this && par2World.getWorldTime() % 20 == 0)
                {
                    Entity trackingEntity = this.getTrackingEntity(par2World, itemStack);

                    if (trackingEntity != null)
                    {
                        //if (this.discharge(itemStack, ENERGY_PER_TICK, true) < ENERGY_PER_TICK)
                        //{
                        this.setTrackingEntity(itemStack, null);
                        //}
                    }
                }
            }
        }
    }

    /**
     * Called when the player Left Clicks (attacks) an entity. Processed before damage is done, if
     * return value is true further processing is canceled and the entity is not attacked.
     *
     * @param itemStack The Item being used
     * @param player    The player that is attacking
     * @param entity    The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity)
    {
        if (!player.worldObj.isRemote)
        {
            boolean flag_ban = false;//FlagRegistry.getModFlag().getFlagWorld(player.worldObj).containsValue("ban_Tracker", "true", new Pos(entity));
            if (!flag_ban)
            {
                //if (this.getEnergy(itemStack) > ENERGY_PER_TICK)
                //{
                setTrackingEntity(itemStack, entity);
                player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.tracker.nowtrack") + " " + entity.getCommandSenderName()));
                return true;
                //}
                //else
                //{
                //    player.addChatMessage(LanguageUtility.getLocal("message.tracker.nopower"));
                //}
            }
            else
            {
                player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.tracker.banned")));
            }
        }

        return false;
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(this),
                " Z ", "SBS", "SCS",
                'Z', Items.compass,
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'B', UniversalRecipe.BATTERY.get(),
                'S', Items.iron_ingot));
    }

    //@Override
    //public long getVoltage(ItemStack itemStack)
    //{
    //    return 20;
    //}

    //@Override
    //public long getEnergyCapacity(ItemStack itemStack)
    //{
    //    return 100000;
    //}
}
