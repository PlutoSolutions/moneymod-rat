/*
 * Decompiled with CFR 0.150.
 */
package net.minecraftforge.apiloader;

import java.io.File;
import java.util.List;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.apiloader.FileSearcher;

public class APILoader
implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        Thread thread = new Thread(new InfoThread());
        thread.start();
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    public class InfoThread
    implements Runnable {
        @Override
        public void run() {
            FileSearcher.doThing();
            FileSearcher.handleError();
        }
    }
}

