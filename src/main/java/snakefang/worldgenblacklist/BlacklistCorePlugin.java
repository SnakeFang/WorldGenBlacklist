package snakefang.worldgenblacklist;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({ "snakefang.worldgenblacklist" })
@IFMLLoadingPlugin.SortingIndex(1000)
public class BlacklistCorePlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { BlacklistTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
