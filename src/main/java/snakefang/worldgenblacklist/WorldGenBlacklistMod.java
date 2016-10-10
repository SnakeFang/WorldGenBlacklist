package snakefang.worldgenblacklist;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import testpackage.TestWorldGenerator;

@Mod(modid = "worldgenblacklist")
public class WorldGenBlacklistMod
{
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.registerWorldGenerator(new TestWorldGenerator(), 1);
    }
}
