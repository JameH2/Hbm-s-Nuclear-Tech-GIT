package com.hbm.asm.Transform;

/*
 * Invaluable help from AtomicStryker's MultiMine coremod code <3
 */

import java.util.ArrayList;
import java.util.List;

//mport com.bioxx.tfc.ASM.ClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.hbm.asm.ClassTransformer;

public class NTM_EntityRenderer extends ClassTransformer
{

	public NTM_EntityRenderer()
	{
		mcpClassName = "net.minecraft.client.renderer.EntityRenderer";
		obfClassName = "blt";

		List<InstrSet> nodes = new ArrayList<InstrSet>();
		InsnList list = new InsnList();
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "random", "Ljava/util/Random;"));
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "rendererUpdateCount", "I"));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hbm/main/ModEventHandlerClient","doRainClient","(Ljava/util/Random;I)V"));
		nodes.add(new InstrSet(list, 208, InstrOpType.Replace));
		this.mcpMethodNodes.put("updateRenderer | ()V", new Patch(nodes, PatchOpType.Modify));

		nodes = new ArrayList<InstrSet>();
		list = new InsnList();
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "blt", "al", "Ljava/util/Random;"));
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "blt", "w", "I"));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hbm/main/ModEventHandlerClient","doRainClient","(Ljava/util/Random;I)V"));
		nodes.add(new InstrSet(list, 208, InstrOpType.Replace));
		this.obfMethodNodes.put("d | ()V", new Patch(nodes, PatchOpType.Modify));
	}
}