/*
 * Decompiled with CFR 0.150.
 */
package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;
import net.minecraft.launchwrapper.LaunchClassLoader;

public interface ITweaker {
    public void acceptOptions(List<String> var1, File var2, File var3, String var4);

    public void injectIntoClassLoader(LaunchClassLoader var1);

    public String getLaunchTarget();

    public String[] getLaunchArguments();
}

