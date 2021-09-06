/*
 * Decompiled with CFR 0.150.
 */
package net.minecraft.launchwrapper.injector;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.injector.VanillaTweakInjector;

public class AlphaVanillaTweakInjector
implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        return bytes;
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz;
        try {
            clazz = AlphaVanillaTweakInjector.getaClass("net.minecraft.client.MinecraftApplet");
        }
        catch (ClassNotFoundException ignored) {
            clazz = AlphaVanillaTweakInjector.getaClass("com.mojang.minecraft.MinecraftApplet");
        }
        System.out.println("AlphaVanillaTweakInjector.class.getClassLoader() = " + AlphaVanillaTweakInjector.class.getClassLoader());
        Constructor<?> constructor = clazz.getConstructor(new Class[0]);
        Object object = constructor.newInstance(new Object[0]);
        for (Field field : clazz.getDeclaredFields()) {
            String name = field.getType().getName();
            if (name.contains("awt") || name.contains("java") || name.equals("long")) continue;
            System.out.println("Found likely Minecraft candidate: " + field);
            Field fileField = AlphaVanillaTweakInjector.getWorkingDirField(name);
            if (fileField == null) continue;
            System.out.println("Found File, changing to " + Launch.minecraftHome);
            fileField.setAccessible(true);
            fileField.set(null, Launch.minecraftHome);
            break;
        }
        AlphaVanillaTweakInjector.startMinecraft((Applet)object, args);
    }

    private static void startMinecraft(final Applet applet, String[] args) {
        HashMap<String, String> params = new HashMap<String, String>();
        String name = "Player" + System.currentTimeMillis() % 1000L;
        if (args.length > 0) {
            name = args[0];
        }
        String sessionId = "-";
        if (args.length > 1) {
            sessionId = args[1];
        }
        params.put("username", name);
        params.put("sessionid", sessionId);
        Frame launcherFrameFake = new Frame();
        launcherFrameFake.setTitle("Minecraft");
        launcherFrameFake.setBackground(Color.BLACK);
        JPanel panel = new JPanel();
        launcherFrameFake.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(854, 480));
        launcherFrameFake.add((Component)panel, "Center");
        launcherFrameFake.pack();
        launcherFrameFake.setLocationRelativeTo(null);
        launcherFrameFake.setVisible(true);
        launcherFrameFake.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });
        class LauncherFake
        extends Applet
        implements AppletStub {
            private static final long serialVersionUID = 1L;
            final /* synthetic */ Map val$params;

            LauncherFake(Map map) {
                this.val$params = map;
            }

            @Override
            public void appletResize(int width, int height) {
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public URL getDocumentBase() {
                try {
                    return new URL("http://www.minecraft.net/game/");
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public URL getCodeBase() {
                try {
                    return new URL("http://www.minecraft.net/game/");
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getParameter(String paramName) {
                if (this.val$params.containsKey(paramName)) {
                    return (String)this.val$params.get(paramName);
                }
                System.err.println("Client asked for parameter: " + paramName);
                return null;
            }
        }
        LauncherFake fakeLauncher = new LauncherFake(params);
        applet.setStub(fakeLauncher);
        fakeLauncher.setLayout(new BorderLayout());
        fakeLauncher.add((Component)applet, "Center");
        fakeLauncher.validate();
        launcherFrameFake.removeAll();
        launcherFrameFake.setLayout(new BorderLayout());
        launcherFrameFake.add((Component)fakeLauncher, "Center");
        launcherFrameFake.validate();
        applet.init();
        applet.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                applet.stop();
            }
        });
        VanillaTweakInjector.loadIconsOnFrames();
    }

    private static Class<?> getaClass(String name) throws ClassNotFoundException {
        return Launch.classLoader.findClass(name);
    }

    private static Field getWorkingDirField(String name) throws ClassNotFoundException {
        Class<?> clazz = AlphaVanillaTweakInjector.getaClass(name);
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !field.getType().getName().equals("java.io.File")) continue;
            return field;
        }
        return null;
    }
}

