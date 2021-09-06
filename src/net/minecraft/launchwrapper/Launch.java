/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  org.apache.logging.log4j.Level
 */
package net.minecraft.launchwrapper;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;

public class Launch {
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String, Object> blackboard;
    public static LaunchClassLoader classLoader;

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    private Launch() {
        URLClassLoader ucl = (URLClassLoader)this.getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
        blackboard = new HashMap<String, Object>();
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void launch(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        ArgumentAcceptingOptionSpec profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        ArgumentAcceptingOptionSpec gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg().defaultsTo((Object)DEFAULT_TWEAK, (Object[])new String[0]);
        NonOptionArgumentSpec nonOption = parser.nonOptions();
        OptionSet options = parser.parse(args);
        minecraftHome = (File)options.valueOf((OptionSpec)gameDirOption);
        assetsDir = (File)options.valueOf((OptionSpec)assetsDirOption);
        String profileName = (String)options.valueOf((OptionSpec)profileOption);
        ArrayList tweakClassNames = new ArrayList(options.valuesOf((OptionSpec)tweakClassOption));
        ArrayList<String> argumentList = new ArrayList<String>();
        blackboard.put("TweakClasses", tweakClassNames);
        blackboard.put("ArgumentList", argumentList);
        HashSet<String> allTweakerNames = new HashSet<String>();
        ArrayList<ITweaker> allTweakers = new ArrayList<ITweaker>();
        try {
            ArrayList<ITweaker> tweakers = new ArrayList<ITweaker>(tweakClassNames.size() + 1);
            blackboard.put("Tweaks", tweakers);
            ITweaker primaryTweaker = null;
            do {
                Iterator it = tweakClassNames.iterator();
                while (it.hasNext()) {
                    String tweakName = (String)it.next();
                    if (allTweakerNames.contains(tweakName)) {
                        LogWrapper.log(Level.WARN, "Tweak class name %s has already been visited -- skipping", tweakName);
                        it.remove();
                        continue;
                    }
                    allTweakerNames.add(tweakName);
                    LogWrapper.log(Level.INFO, "Loading tweak class name %s", tweakName);
                    classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf(46)));
                    ITweaker tweaker = (ITweaker)Class.forName(tweakName, true, classLoader).newInstance();
                    tweakers.add(tweaker);
                    it.remove();
                    if (primaryTweaker != null) continue;
                    LogWrapper.log(Level.INFO, "Using primary tweak class name %s", tweakName);
                    primaryTweaker = tweaker;
                }
                it = tweakers.iterator();
                while (it.hasNext()) {
                    ITweaker tweaker = (ITweaker)it.next();
                    LogWrapper.log(Level.INFO, "Calling tweak class %s", tweaker.getClass().getName());
                    tweaker.acceptOptions(options.valuesOf((OptionSpec)nonOption), minecraftHome, assetsDir, profileName);
                    tweaker.injectIntoClassLoader(classLoader);
                    allTweakers.add(tweaker);
                    it.remove();
                }
            } while (!tweakClassNames.isEmpty());
            for (ITweaker tweaker : allTweakers) {
                argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments()));
            }
            String launchTarget = primaryTweaker.getLaunchTarget();
            Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Method mainMethod = clazz.getMethod("main", String[].class);
            LogWrapper.info("Launching wrapped minecraft {%s}", launchTarget);
            mainMethod.invoke(null, new Object[]{argumentList.toArray(new String[argumentList.size()])});
        }
        catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "Unable to launch", new Object[0]);
            System.exit(1);
        }
    }
}

