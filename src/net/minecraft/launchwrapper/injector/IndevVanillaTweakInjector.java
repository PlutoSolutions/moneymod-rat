/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassReader
 *  org.objectweb.asm.ClassVisitor
 *  org.objectweb.asm.ClassWriter
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.JumpInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TableSwitchInsnNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package net.minecraft.launchwrapper.injector;

import java.io.File;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.injector.VanillaTweakInjector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class IndevVanillaTweakInjector
implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept((ClassVisitor)classNode, 8);
        if (!classNode.interfaces.contains("java/lang/Runnable")) {
            return bytes;
        }
        MethodNode runMethod = null;
        for (MethodNode methodNode : classNode.methods) {
            if (!"run".equals(methodNode.name)) continue;
            runMethod = methodNode;
            break;
        }
        if (runMethod == null) {
            return bytes;
        }
        System.out.println("Probably the Minecraft class (it has run && is applet!): " + name);
        ListIterator iterator = runMethod.instructions.iterator();
        int firstSwitchJump = -1;
        while (iterator.hasNext()) {
            AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();
            if (instruction.getOpcode() == 170) {
                TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode)instruction;
                firstSwitchJump = runMethod.instructions.indexOf((AbstractInsnNode)tableSwitchInsnNode.labels.get(0));
                continue;
            }
            if (firstSwitchJump < 0 || runMethod.instructions.indexOf(instruction) != firstSwitchJump) continue;
            int endOfSwitch = -1;
            while (iterator.hasNext()) {
                instruction = (AbstractInsnNode)iterator.next();
                if (instruction.getOpcode() != 167) continue;
                endOfSwitch = runMethod.instructions.indexOf((AbstractInsnNode)((JumpInsnNode)instruction).label);
                break;
            }
            if (endOfSwitch < 0) continue;
            while (runMethod.instructions.indexOf(instruction) != endOfSwitch && iterator.hasNext()) {
                instruction = (AbstractInsnNode)iterator.next();
            }
            instruction = (AbstractInsnNode)iterator.next();
            runMethod.instructions.insertBefore(instruction, (AbstractInsnNode)new MethodInsnNode(184, "net/minecraft/launchwrapper/injector/IndevVanillaTweakInjector", "inject", "()Ljava/io/File;"));
            runMethod.instructions.insertBefore(instruction, (AbstractInsnNode)new VarInsnNode(58, 2));
        }
        ClassWriter writer = new ClassWriter(3);
        classNode.accept((ClassVisitor)writer);
        return writer.toByteArray();
    }

    public static File inject() {
        System.out.println("Turning off ImageIO disk-caching");
        ImageIO.setUseCache(false);
        VanillaTweakInjector.loadIconsOnFrames();
        System.out.println("Setting gameDir to: " + Launch.minecraftHome);
        return Launch.minecraftHome;
    }
}

