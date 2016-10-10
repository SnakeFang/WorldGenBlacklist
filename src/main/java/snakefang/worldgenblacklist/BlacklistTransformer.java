package snakefang.worldgenblacklist;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
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
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class BlacklistTransformer implements IClassTransformer
{
    private static InsnList generatorCheck;
    private static LabelNode oldMethodBegin;
    
    static
    {
        Field[] worldFields = World.class.getFields();
        
        String providerFieldName = "";
        
        for (Field field : worldFields)
        {
            if(field.getType().isAssignableFrom(WorldProvider.class))
            {
                providerFieldName = field.getName();
            }
        }
        
        generatorCheck = new InsnList();
        oldMethodBegin = new LabelNode();
        
        generatorCheck.add(new VarInsnNode(Opcodes.ALOAD, 4));
        generatorCheck.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(World.class), providerFieldName, Type.getDescriptor(WorldProvider.class)));
        generatorCheck.add(new TypeInsnNode(Opcodes.INSTANCEOF, Type.getInternalName(WorldProviderSurface.class)));
        generatorCheck.add(new JumpInsnNode(Opcodes.IFEQ, oldMethodBegin));
        generatorCheck.add(new InsnNode(Opcodes.RETURN));
        generatorCheck.add(oldMethodBegin);
    }
    
    private static InsnList createGeneratorCheck()
    {
        InsnList instructions = new InsnList();
        
        Map<LabelNode, LabelNode> labelNodeMap = Collections.singletonMap(oldMethodBegin, new LabelNode());
        
        for(AbstractInsnNode instruction : generatorCheck.toArray())
        {
            instructions.add(instruction.clone(labelNodeMap));
        }
        
        return instructions;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        
        if(transformedName.contains("IWorldGenerator"))
        {
            System.out.println(classNode.methods);
        }
        
        if((classNode.access & Opcodes.ACC_INTERFACE) == 0 && classNode.interfaces.contains(Type.getInternalName(IWorldGenerator.class)))
        {
            System.out.println("Transforming: " + transformedName);
            
            Type returnType = Type.VOID_TYPE;
            Type[] argumentTypes = 
            {
                Type.getType(Random.class),
                Type.INT_TYPE,
                Type.INT_TYPE,
                Type.getType(World.class),
                Type.getType(IChunkGenerator.class),
                Type.getType(IChunkProvider.class)
            };
            
            for(MethodNode method : classNode.methods)
            {
                if((method.access & Opcodes.ACC_ABSTRACT) == 0 && method.name.equals("generate") && method.desc.equals(Type.getMethodDescriptor(returnType, argumentTypes)))
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
