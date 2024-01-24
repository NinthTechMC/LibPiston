package libpiston.coremod;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LibPistonCore implements IFMLLoadingPlugin {

    static {
        System.out.println("LibPistonCore loaded");
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
