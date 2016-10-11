package snakefang.worldgenblacklist;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.fml.common.IWorldGenerator;

public class BlacklistTransformer implements IClassTransformer
{
    // Streams: making things as complicated as possible since forever
    private static String worldProviderFieldName = Arrays.asList(World.class.getFields()).stream().filter(field -> field.getType().isAssignableFrom(WorldProvider.class)).findAny().orElseThrow(IllegalStateException::new).getName();
    private static Method worldGeneratorMethod = Arrays.asList(IWorldGenerator.class.getMethods()).stream().filter(method -> method.getName().equals("generate")).findAny().orElseThrow(IllegalStateException::new);
    
    private static InsnList createGeneratorCheck()
    {
        InsnList generatorCheck = new InsnList();
        LabelNode methodBegin = new LabelNode();
        
        generatorCheck.add(new VarInsnNode(Opcodes.ALOAD, 4));
        generatorCheck.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(World.class), worldProviderFieldName, Type.getDescriptor(WorldProvider.class)));
        generatorCheck.add(new TypeInsnNode(Opcodes.INSTANCEOF, Type.getInternalName(WorldProviderSurface.class)));
        generatorCheck.add(new JumpInsnNode(Opcodes.IFEQ, methodBegin));
        generatorCheck.add(new InsnNode(Opcodes.RETURN));
        generatorCheck.add(methodBegin);
        
        return generatorCheck;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        
        if((classNode.access & Opcodes.ACC_INTERFACE) == 0 && classNode.interfaces.contains(Type.getInternalName(IWorldGenerator.class)))
        {
            System.out.println(transformedName);
            
            for(MethodNode method : classNode.methods)
            {
                if((method.access & Opcodes.ACC_ABSTRACT) == 0 && method.name.equals("generate") && method.desc.equals(Type.getMethodDescriptor(worldGeneratorMethod)))
                {
                    method.instructions.insert(createGeneratorCheck());
                }
            }
            
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            
            return classWriter.toByteArray();
        }
        
        return basicClass;
    }
}
