package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.client.effects.providers.VEProviderShockWave;
import com.builtbroken.mc.framework.energy.UniversalEnergySystem;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.world.map.radar.RadarRegistry;

import appeng.tile.powersink.AEBasePoweredTile;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosive;
import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import physica.forcefield.PhysicaForcefields;
import physica.forcefield.common.tile.TileFortronField;
import physica.forcefield.common.tile.TileFortronFieldConstructor;
import resonant.api.explosion.IEMPBlock;
import resonant.api.explosion.IMissile;

import java.util.List;

public class BlastEMP extends Blast
{
    private boolean effectEntities = false;
    private boolean effectBlocks = false;

    public BlastEMP(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastEMP setEffectBlocks()
    {
        this.effectBlocks = true;
        return this;
    }

    public BlastEMP setEffectEntities()
    {
        this.effectEntities = true;
        return this;
    }

    @Override
    public void doExplode()
    {
        if(!oldWorld().isRemote)
        {
            if (this.effectBlocks)
            {
            	ArrayList<TileFortronFieldConstructor> constructors = PhysicaForcefields.getRelevantConstructors(oldWorld(), x(), y(), z())
            	
                for (int x = (int) -this.getRadius(); x < (int) this.getRadius(); x++)
                {
                    for (int y = (int) -this.getRadius(); y < (int) this.getRadius(); y++)
                    {
                        for (int z = (int) -this.getRadius(); z < (int) this.getRadius(); z++)
                        {
                        	for (TileFortronFieldConstructor constructor : constructors) {
                            	if (constructor.isProtecting(x, y, z))
                            		System.out.println("Constructor is protecting block");
                            		constructor.damageForcefield(30);
                            }
                            double dist = MathHelper.sqrt_double((x * x + y * y + z * z));

                            Pos searchPosition = new Pos(x, y, z).add(position);
                            if (dist > this.getRadius())
                            {
                                continue;
                            }

                            if (Math.round(position.x() + y) == position.yi())
                            {
                                oldWorld().spawnParticle("largesmoke", searchPosition.x(), searchPosition.y(), searchPosition.z(), 0, 0, 0);
                            }

                            Block block = searchPosition.getBlock(oldWorld());
                            TileEntity tileEntity = searchPosition.getTileEntity(oldWorld());
                            //TODO fire EMP event
                            //TODO more EMP effect to UniversalEnergySystem to better support cross mod support
                            if (block != null)
                            {
                                if (block instanceof IEMPBlock)
                                {
                                    ((IEMPBlock) block).onEMP(oldWorld(), searchPosition.xi(), searchPosition.yi(), searchPosition.zi(), this);
                                }
                            }

                            if (tileEntity != null)
                            {
                            	if (tileEntity instanceof TileFortronField) {
                            		TileFortronFieldConstructor ffTile = (TileFortronFieldConstructor) ((TileFortronField) tileEntity).getConstructorCoord().getTile(oldWorld());
                            		ffTile.damageForcefield(100);
                            	}
                            	else if (tileEntity instanceof AEBasePoweredTile) {
                            		AEBasePoweredTile storage = (AEBasePoweredTile) tileEntity;
                            		storage.setInternalCurrentPower(0.0d);
                            		storage.markForUpdate();
                            	} else if (tileEntity instanceof IStrictEnergyStorage) {
                            		IStrictEnergyStorage storage = (IStrictEnergyStorage) tileEntity;
                            		storage.setEnergy(0.0d);
                            	}
                            	else
                            	{
                            		NBTTagCompound tags = new NBTTagCompound();
                            		tileEntity.writeToNBT(tags);
                            		if (!tags.hasNoTags()) {
                            			if (tags.hasKey("electricityStored") && tags.getString("tier") != "Creative")
                            			{
                            				tags.setDouble("electricityStored", 0.0d);
                            			}
                            			else if (tags.hasKey("EnergyF"))
                            			{
                            				tags.setFloat("EnergyF", 0.0f);
                            			}
                            			else if (tags.hasKey("Energy"))
                            			{
                            				tags.setDouble("Energy", 0.0d);
                            			}
                            			else if (tags.hasKey("energyStorage"))
                            			{
                            				tags.getCompoundTag("energyStorage").setInteger("Energy", 0);
                            			}
                            		}
                            		tileEntity.readFromNBT(tags);
                            		UniversalEnergySystem.clearEnergy(tileEntity, true);
                            	}
                            }
                        }
                    }
                }
            }

            if (this.effectEntities)
            {
                // Drop all missiles
                List<Entity> entitiesNearby = RadarRegistry.getAllLivingObjectsWithin(oldWorld(), new Cube(position.sub(getRadius()), position.add(getRadius())), null);

                for (Entity entity : entitiesNearby)
                {
                    if (entity instanceof IMissile && !entity.isEntityEqual(this.controller) && entity.isEntityAlive())
                    {
                        if (((IMissile) entity).getTicksInAir() > -1)
                        {
                            ((IMissile) entity).dropMissileAsItem();
                        }
                    }
                }

                int maxFx = 10;
                AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(position.x() - this.getRadius(), position.y() - this.getRadius(), position.z() - this.getRadius(), position.x() + this.getRadius(), position.y() + this.getRadius(), position.z() + this.getRadius());
                List<Entity> entities = oldWorld().getEntitiesWithinAABB(Entity.class, bounds);

                for (Entity entity : entities)
                {
                    if (entity instanceof EntityLivingBase)
                    {
                        if (this.oldWorld().isRemote && maxFx > 0)
                        {
                            ICBMClassic.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
                            maxFx--;
                        }

                        if (entity instanceof EntityCreeper)
                        {
                            if (!this.oldWorld().isRemote)
                            {
                                try
                                {
                                    ((EntityCreeper) entity).getDataWatcher().updateObject(17, (byte) 1);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (entity instanceof EntityPlayer)
                        {
                            IInventory inventory = ((EntityPlayer) entity).inventory;

                            for (int i = 0; i < inventory.getSizeInventory(); i++)
                            {
                                ItemStack itemStack = inventory.getStackInSlot(i);

                                if (itemStack != null)
                                {
                                	NBTTagCompound tags = itemStack.getTagCompound();
                                	if (tags != null) {
                                		// Modular Powersuits
                                		if (tags.hasKey("mmmpsmod"))
                                		{
                                			tags.getCompoundTag("mmmpsmod").setDouble("Current Energy", 0.0d);
                                		}
                                		// Mekanism/galacticraft
                                		else if (tags.hasKey("electricity"))
                                		{
                                			tags.setDouble("electricity", 0.0d);
                                		}
                                		// Applied Energistics 2
                                		else if (tags.hasKey("power"))
                                		{
                                			tags.setDouble("power", 0.0d);
                                		}
                                		else if (tags.hasKey("energy"))
                                		{
                                			tags.setDouble("energy", 0.0d);
                                		}
                                		else if (tags.hasKey("Energy"))
                                		{
                                			tags.setDouble("Energy", 0.0d);
                                		}
                                		// Redstone Flux
                                		else if (tags.hasKey("internalCurrentPower"))
                                		{
                                			tags.setDouble("internalCurrentPower", 0.0d);
                                		}
                                	}
                                	itemStack.setTagCompound(tags);
                                	/*
                                    if (itemStack.getItem() instanceof IEMPItem)
                                    {
                                        ((IEMPItem) itemStack.getItem()).onEMP(itemStack, entity, this);
                                    }
                                    UniversalEnergySystem.clearEnergy(itemStack, true);
                                    */
                                }
                            }
                        }
                    }
                    else if (entity instanceof EntityExplosive)
                    {
                        entity.setDead();
                    }
                }
            }

            VEProviderShockWave.spawnEffect(oldWorld(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 1, 3);
            VEProviderShockWave.spawnEffect(oldWorld(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 3, 3);
            VEProviderShockWave.spawnEffect(oldWorld(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 5, 3);
            this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "emp", 4.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
