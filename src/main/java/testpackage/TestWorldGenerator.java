package testpackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class TestWorldGenerator implements IWorldGenerator
{
    private List<Integer> printed = new ArrayList<Integer>();
    
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if(!printed.contains(world.provider.getDimension()))
        {
            System.out.println(world.provider.getClass().getName());
            printed.add(world.provider.getDimension());
        }
    }
}
