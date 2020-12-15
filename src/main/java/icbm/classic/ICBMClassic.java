package icbm.classic;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.registry.ModManager;
import com.builtbroken.mc.framework.mod.AbstractMod;
import com.builtbroken.mc.framework.mod.AbstractProxy;
import com.builtbroken.mc.framework.mod.ModCreativeTab;
import com.builtbroken.mc.framework.mod.Mods;
import com.builtbroken.mc.framework.mod.loadable.LoadableHandler;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.prefab.items.ItemBlockBase;
import com.builtbroken.mc.prefab.items.ItemBlockSubTypes;
import com.builtbroken.mc.seven.framework.block.json.JsonBlockListenerProcessor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.content.DataSystemICBMLoader;
import icbm.classic.content.blocks.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.missiles.Missile;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.content.items.*;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.potion.PoisonContagion;
import icbm.classic.content.potion.PoisonFrostBite;
import icbm.classic.content.potion.PoisonToxin;
import icbm.classic.mod.IC2Proxy.IC2Proxy;
import icbm.classic.mod.ModProxy;
import icbm.classic.mod.waila.WailaLoader;
import icbm.classic.prefab.ListenerExplosiveBreakTrigger;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main class for ICBM core to run on. The core will need to be initialized by each ICBM module.
 *
 * @author Calclavia
 */
@Mod(modid = ICBMClassic.DOMAIN, name = "ICBM-Classic", version = "2.16.4.3", dependencies = ICBMClassic.DEPENDENCIES)
public final class ICBMClassic extends AbstractMod
{
    @Instance(ICBMClassic.DOMAIN)
    public static ICBMClassic INSTANCE;

    @Metadata(ICBMClassic.DOMAIN)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "icbm.classic.ClientProxy", serverSide = "icbm.classic.ServerProxy")
    public static CommonProxy proxy;

    public static final String DOMAIN = "icbmclassic";
    public static final String PREFIX = DOMAIN + ":";

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    public static final String DEPENDENCIES = "required-after:voltzengine;after:OpenComputers";

    public static final int ENTITY_ID_PREFIX = 50;

    //Mod support
    public static Block blockRadioactive;
    public static int blockRadioactiveMeta;

    // Blocks
    public static Block blockGlassPlate;
    public static Block blockGlassButton;
    public static Block blockProximityDetector;
    public static Block blockSpikes;
    public static Block blockCamo;
    public static Block blockConcrete;
    public static Block blockReinforcedGlass;
    public static Block blockExplosive;
    public static Block blockCombatRail;

    public static Block blockLaunchBase;
    public static Block blockLaunchScreen;
    public static Block blockLaunchSupport;
    public static Block blockRadarStation;
    public static Block blockEmpTower;
    public static Block blockCruiseLauncher;
    public static Block blockMissileCoordinator;


    // Items
    public static Item itemAntidote;
    public static Item itemSignalDisrupter;
    public static Item itemTracker;
    public static Item itemMissile;
    public static Item itemDefuser;
    public static Item itemRadarGun;
    public static Item itemRemoteDetonator;
    public static Item itemLaserDesignator;
    public static Item itemRocketLauncher;
    public static Item itemGrenade;
    public static Item itemBombCart;
    public static Item itemSulfurDust;
    public static Item itemPoisonPowder;

    public static final ContagiousPoison poisonous_potion = new ContagiousPoison("Chemical", 1, false);
    public static final ContagiousPoison contagios_potion = new ContagiousPoison("Contagious", 1, true);

    public final ModCreativeTab CREATIVE_TAB;

    public static ModProxy IC2PROXY;

    public ICBMClassic()
    {
        super(ICBMClassic.DOMAIN, "/bbm/ICBM-Classic");
        CREATIVE_TAB = new ModCreativeTab(DOMAIN);
        getManager().setTab(CREATIVE_TAB);
    }

    @Override
    public void loadJsonContentHandlers()
    {
        super.loadJsonContentHandlers();
        JsonBlockListenerProcessor.addBuilder(new ListenerExplosiveBreakTrigger.Builder());
    }

    @Override
    public void loadHandlers(LoadableHandler loader)
    {
        loader.applyModule(WailaLoader.class, Mods.WAILA.isLoaded());
        loader.applyModule(IC2Proxy.class, Mods.IC2.isLoaded());
    }

    @Override
    protected void loadBlocks(ModManager manager)
    {
        blockGlassPlate = manager.newBlock("icbmCGlassPlate", BlockGlassPressurePlate.class, ItemBlockBase.class);
        blockGlassButton = manager.newBlock("icbmCGlassButton", BlockGlassButton.class, ItemBlockBase.class);
        blockSpikes = manager.newBlock("icbmCSpike", BlockSpikes.class, ItemBlockSubTypes.class);
        blockCamo = manager.newBlock("icbmCCamouflage", TileCamouflage.class);
        blockConcrete = manager.newBlock("icbmCConcrete", BlockConcrete.class, ItemBlockSubTypes.class);
        blockReinforcedGlass = manager.newBlock("icbmCGlass", BlockReinforcedGlass.class, ItemBlockBase.class);
        blockCombatRail = manager.newBlock("icbmCRail", BlockReinforcedRail.class, ItemBlockBase.class);
        blockExplosive = manager.newBlock("icbmCExplosive", BlockExplosive.class, ItemBlockExplosive.class);
    }

    @Override
    public void loadItems(ModManager manager)
    {
        itemPoisonPowder = manager.newItem("icbmCPoisonPowder", new ItemICBMBase("poisonPowder"));
        itemSulfurDust = manager.newItem("icbmCSulfurDust", ItemSulfurDust.class);
        itemAntidote = manager.newItem("icbmCAntidote", ItemAntidote.class);
        itemSignalDisrupter = manager.newItem("icbmCSignalDisrupter", ItemSignalDisrupter.class);
        itemTracker = manager.newItem("icbmCTracker", ItemTracker.class);
        itemMissile = manager.newItem("icbmCMissile", ItemMissile.class);
        itemDefuser = manager.newItem("icbmCDefuser", ItemDefuser.class);
        itemRadarGun = manager.newItem("icbmCRadarGun", ItemRadarGun.class);
        itemRemoteDetonator = manager.newItem("icbmCRemoteDetonator", ItemRemoteDetonator.class);
        itemLaserDesignator = manager.newItem("icbmCLaserDetonator", ItemLaserDetonator.class);
        itemRocketLauncher = manager.newItem("icbmCRocketLauncher", ItemRocketLauncher.class);
        itemGrenade = manager.newItem("icbmCGrenade", ItemGrenade.class);
        itemBombCart = manager.newItem("icbmCBombCart", ItemBombCart.class);

        CREATIVE_TAB.itemStack = new ItemStack(itemMissile);
    }

    @Override
    public void loadEntities(ModManager manager)
    {
        //EntityRegistry.registerGlobalEntityID(EntityFlyingBlock.class, "ICBMGravityBlock", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityFragments.class, "ICBMFragment", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityExplosive.class, "ICBMExplosive", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityMissile.class, "ICBMMissile", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityExplosion.class, "ICBMProceduralExplosion", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityLightBeam.class, "ICBMLightBeam", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityGrenade.class, "ICBMGrenade", EntityRegistry.findGlobalUniqueEntityId());
        //EntityRegistry.registerGlobalEntityID(EntityBombCart.class, "ICBMChe", EntityRegistry.findGlobalUniqueEntityId());

        int nextID = ENTITY_ID_PREFIX;
        EntityRegistry.registerModEntity(EntityFlyingBlock.class, "ICBMGravityBlock", nextID++, this, 50, 15, true);
        EntityRegistry.registerModEntity(EntityFragments.class, "ICBMFragment", nextID++, this, 40, 8, true);
        EntityRegistry.registerModEntity(EntityExplosive.class, "ICBMExplosive", nextID++, this, 50, 5, true);
        EntityRegistry.registerModEntity(EntityMissile.class, "ICBMMissile", nextID++, this, 500, 1, true);
        EntityRegistry.registerModEntity(EntityExplosion.class, "ICBMProceduralExplosion", nextID++, this, 100, 5, true);
        EntityRegistry.registerModEntity(EntityLightBeam.class, "ICBMLightBeam", nextID++, this, 80, 5, true);
        EntityRegistry.registerModEntity(EntityGrenade.class, "ICBMGrenade", nextID++, this, 50, 5, true);
        EntityRegistry.registerModEntity(EntityBombCart.class, "ICBMChe", nextID++, this, 50, 2, true);
        EntityRegistry.registerModEntity(EntityPlayerSeat.class, "ICBMSeat", nextID++, this, 50, 2, true);
    }

    @Override
    public void loadRecipes(ModManager manager)
    {
/** LOAD. */
        ArrayList dustCharcoal = OreDictionary.getOres("dustCharcoal");
        ArrayList dustCoal = OreDictionary.getOres("dustCoal");
        // Sulfur
        //GameRegistry.addSmelting(blockSulfurOre, new ItemStack(itemSulfurDust, 4), 0.8f);
        GameRegistry.addSmelting(Items.reeds, new ItemStack(itemSulfurDust, 4, 1), 0f);
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", Items.coal));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", new ItemStack(Items.coal, 1, 1)));

        if (dustCharcoal != null && dustCharcoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCharcoal"));
        }
        if (dustCoal != null && dustCoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCoal"));
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(Blocks.tnt,
                "@@@", "@R@", "@@@",
                '@', Items.gunpowder,
                'R', Items.redstone));

        // Poison Powder
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemPoisonPowder, 3), Items.spider_eye, Items.rotten_flesh));

        // Glass Pressure Plate
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockGlassPlate, 1, 0), "##", '#', Blocks.glass));

        // Glass Button
        GameRegistry.addRecipe(new ItemStack(blockGlassButton, 2), "G", "G", 'G', Blocks.glass);

        // Proximity Detector
        GameRegistry.addRecipe(new ShapedOreRecipe(blockProximityDetector,
                "SSS", "S?S", "SSS",
                'S', Items.iron_ingot,
                '?', itemTracker));

        // Signal Disrupter
        GameRegistry.addRecipe(new ShapedOreRecipe(itemSignalDisrupter,
                "WWW", "SCS", "SSS",
                'S', Items.iron_ingot,
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'W', UniversalRecipe.WIRE.get()));

        // Antidote
        OreDictionary.registerOre("seeds", Items.wheat_seeds);
        OreDictionary.registerOre("seeds", Items.pumpkin_seeds);
        OreDictionary.registerOre("seeds", Items.melon_seeds);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemAntidote, 6),
                "@@@", "@@@", "@@@",
                '@', "seeds"));


        // Reinforced rails
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCombatRail, 16, 0), new Object[]{"C C", "CIC", "C C", 'I', new ItemStack(blockConcrete, 1, 0), 'C', Items.iron_ingot}));

        // Reinforced Glass
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockReinforcedGlass, 8), new Object[]{"IGI", "GIG", "IGI", 'G', Blocks.glass, 'I', Items.iron_ingot}));

        // Rocket Launcher
        GameRegistry.addRecipe(new ShapedOreRecipe(itemRocketLauncher,
                "SCR", "SB ",
                'R', itemRadarGun,
                'C', new ItemStack(blockCruiseLauncher),
                'B', Blocks.stone_button,
                'S', UniversalRecipe.PRIMARY_METAL.get()));
        // Radar Gun
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRadarGun),
                "@#!", " $!", "  !",
                '@', Blocks.glass,
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '#', UniversalRecipe.CIRCUIT_T1.get(),
                '$', Blocks.stone_button));
        // Remote
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRemoteDetonator),
                "?@@", "@#$", "@@@",
                '@', UniversalRecipe.PRIMARY_METAL.get(),
                '?', Items.redstone,
                '#', UniversalRecipe.CIRCUIT_T2.get(),
                '$', Blocks.stone_button));
        // Laser Designator
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemLaserDesignator),
                "!  ", " ? ", "  @",
                '@', itemRemoteDetonator,
                '?', UniversalRecipe.CIRCUIT_T3.get(),
                '!', itemRadarGun));
        // Defuser
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemDefuser),
                "I  ", " W ", "  C",
                'C', UniversalRecipe.CIRCUIT_T2.get(),
                'W', UniversalRecipe.WRENCH.get(),
                'I', UniversalRecipe.WIRE.get()));

        // Missile module
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE.getItemStack(),
                " @ ", "@#@", "@?@",
                '@', UniversalRecipe.PRIMARY_METAL.get(),
                '?', Items.flint_and_steel,
                '#', UniversalRecipe.CIRCUIT_T1.get()));
        // Homing
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_HOMING.getItemStack(),
                " B ", " C ", "BMB",
                'M', Explosives.MISSILE.getItemStack(),
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'B', UniversalRecipe.SECONDARY_METAL.get()));
        // Anti-ballistic
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_ANTI.getItemStack(),
                "!", "?", "@",
                '@', Explosives.MISSILE.getItemStack(),
                '?', new ItemStack(blockExplosive, 1, 0),
                '!', UniversalRecipe.CIRCUIT_T1.get()));
        // Cluster
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_CLUSTER.getItemStack(),
                " ! ", " ? ", "!@!",
                '@', Explosives.MISSILE.getItemStack(),
                '?', Explosives.FRAGMENTATION.getItemStack(),
                '!', new ItemStack(itemMissile, 1, 0)));
        // Nuclear Cluster
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_CLUSTER_NUKE.getItemStack(),
                " N ", "NCN",
                'C', Explosives.MISSILE_CLUSTER.getItemStack(),
                'N', Explosives.NUCLEAR.getItemStack()));

        for (Explosives ex : Explosives.values())
        {
            Explosive explosive = ex.handler;
            explosive.init();

            if (!(explosive instanceof Missile))
            {
                // Missile
                RecipeUtility.addRecipe(new ShapelessOreRecipe(new ItemStack(itemMissile, 1, ex.ordinal()),
                        Explosives.MISSILE.getItemStack(),
                        new ItemStack(blockExplosive, 1, ex.ordinal())), explosive.getUnlocalizedName() + " Missile", getConfig(), true);
                if (explosive.getTier() < 2)
                {
                    // Grenade
                    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemGrenade, 1, ex.ordinal()),
                            "?", "@",
                            '@', new ItemStack(blockExplosive, 1, ex.ordinal()),
                            '?', Items.string), explosive.getUnlocalizedName() + " Grenade", getConfig(), true);
                }
                // Minecart
                RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemBombCart, 1, ex.ordinal()),
                        "?", "@",
                        '?', new ItemStack(blockExplosive, 1, ex.ordinal()),
                        '@', Items.minecart), explosive.getUnlocalizedName() + " Minecart", getConfig(), true);

            }
        }
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        loader.applyModule(new DataSystemICBMLoader());
        Engine.requestBaseModules();
        Engine.requestMultiBlock();
        Settings.load(getConfig());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        setModMetadata(ICBMClassic.DOMAIN, "ICBM-Classic", metadata);

        OreDictionary.registerOre("dustSulfur", new ItemStack(itemSulfurDust, 1, 0));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemSulfurDust, 1, 1));

        /** Check for existence of radioactive block. If it does not exist, then create it. */
        if (OreDictionary.getOres("blockRadioactive").size() > 0)
        {
            ArrayList<ItemStack> stacks = OreDictionary.getOres("blockRadioactive");
            for (ItemStack stack : stacks)
            {
                if (stack != null && stack.getItem() instanceof ItemBlock)
                {
                    //TODO add code to handle this from the ItemStack or test if this block is valid
                    //      As assuming the metadata is valid may not be a good idea, and the block may not be valid as well
                    //TODO add config to force block that is used
                    //TODO add error checking
                    blockRadioactive = ((ItemBlock) stack.getItem()).field_150939_a;
                    blockRadioactiveMeta = ((ItemBlock) stack.getItem()).getMetadata(stack.getItemDamage());
                    logger().info("Detected radioative block from another mod.");
                    logger().info("Radioactive explosives will use: " + blockRadioactive);
                }
            }
        }

        if (blockRadioactive == null)
        {
            blockRadioactive = Blocks.mycelium;
        }

        /** Potion Effects */ //TODO move to effect system
        PoisonToxin.INSTANCE = new PoisonToxin(true, 5149489, "toxin");
        PoisonContagion.INSTANCE = new PoisonContagion(false, 5149489, "virus");
        PoisonFrostBite.INSTANCE = new PoisonFrostBite(false, 5149489, "frostBite");

        /** Dispenser Handler */ //TODO move to its own class
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemGrenade, new IBehaviorDispenseItem()
        {
            @Override
            public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
            {
                World world = blockSource.getWorld();

                if (!world.isRemote)
                {
                    int x = blockSource.getXInt();
                    int y = blockSource.getYInt();
                    int z = blockSource.getZInt();
                    EnumFacing enumFacing = EnumFacing.getFront(blockSource.getBlockMetadata());

                    EntityGrenade entity = new EntityGrenade(world, new Pos(x, y, z), Explosives.get(itemStack.getItemDamage()));
                    entity.setThrowableHeading(enumFacing.getFrontOffsetX(), 0.10000000149011612D, enumFacing.getFrontOffsetZ(), 0.5F, 1.0F);
                    world.spawnEntityInWorld(entity);
                }

                itemStack.stackSize--;
                return itemStack;
            }
        });

        //TODO move to its own class
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemBombCart, new IBehaviorDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultItemDispenseBehavior = new BehaviorDefaultDispenseItem();

            @Override
            public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
            {
                World world = blockSource.getWorld();

                if (!world.isRemote)
                {
                    int x = blockSource.getXInt();
                    int y = blockSource.getYInt();
                    int z = blockSource.getZInt();

                    EnumFacing var3 = EnumFacing.getFront(blockSource.getBlockMetadata());
                    World var4 = blockSource.getWorld();
                    double var5 = blockSource.getX() + var3.getFrontOffsetX() * 1.125F;
                    double var7 = blockSource.getY();
                    double var9 = blockSource.getZ() + var3.getFrontOffsetZ() * 1.125F;
                    int var11 = blockSource.getXInt() + var3.getFrontOffsetX();
                    int var12 = blockSource.getYInt();
                    int var13 = blockSource.getZInt() + var3.getFrontOffsetZ();
                    Block var14 = var4.getBlock(var11, var12, var13);
                    double var15;

                    if (BlockRailBase.func_150051_a(var14))
                    {
                        var15 = 0.0D;
                    }
                    else
                    {
                        if (var14 == Blocks.air || !BlockRailBase.func_150051_a(var4.getBlock(var11, var12 - 1, var13)))
                        {
                            return this.defaultItemDispenseBehavior.dispense(blockSource, itemStack);
                        }

                        var15 = -1.0D;
                    }

                    EntityBombCart var22 = new EntityBombCart(world, var5, var7 + var15, var9, Explosives.get(itemStack.getItemDamage()));
                    world.spawnEntityInWorld(var22);
                    world.playAuxSFX(1000, x, y, z, 0);
                }

                itemStack.stackSize--;
                return itemStack;
            }
        });
        proxy.init();
    }


    public void setModMetadata(String id, String name, ModMetadata metadata)
    {
        metadata.modId = id;
        metadata.name = name;
        metadata.description = "ICBM is a Minecraft Mod that introduces intercontinental ballistic missiles to Minecraft. But the fun doesn't end there! This mod also features many different explosives, missiles and machines classified in three different tiers. If strategic warfare, carefully coordinated airstrikes, messing with matter and general destruction are up your alley, then this mod is for you!";
        metadata.url = "http://www.builtbroken.com/";
        metadata.logoFile = "/icbm_logo.png";
        metadata.version = ICBMClassic.VERSION;
        metadata.authorList = Arrays.asList(new String[]{"Calclavia", "DarkGuardsman aka Darkcow"});
        metadata.credits = "Please visit the website.";
        metadata.autogenerated = false;
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandICBM());
    }
}