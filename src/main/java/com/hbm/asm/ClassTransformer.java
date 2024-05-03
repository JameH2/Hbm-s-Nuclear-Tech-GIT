package com.hbm.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hbm.main.NTMLoadingPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ClassTransformer implements net.minecraft.launchwrapper.IClassTransformer
{
	public static final Logger LOG = LogManager.getLogger("NTM ASM");
	protected Map<String, Patch> mcpMethodNodes = new HashMap<String, Patch>();
	protected Map<String, Patch> obfMethodNodes = new HashMap<String, Patch>();
	protected String mcpClassName;
	protected String obfClassName;


	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.equals(obfClassName))
		{
			return transform(bytes);
		}
		else if (name.equals(mcpClassName))
		{
			return transform(bytes);
		}

		return bytes;
	}

	public static int numInsertions;
	protected byte[] transform(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		LOG.info("Attempting to Transform: " + classNode.name + " | Found " + getMethodNodeList().size() + " injections");
		// find method to inject into
		Iterator<MethodNode> methods = classNode.methods.iterator();
		while (methods.hasNext())
		{
			MethodNode m = methods.next();
			if (getMethodNodeList().containsKey(m.name + " | " + m.desc))
			{
				numInsertions = 0;
				Patch mPatch = getMethodNodeList().get(m.name + " | " + m.desc);
				List<InstrSet> instructions = mPatch.instructions;
				InstrSet target = null;
				if (!instructions.isEmpty())
				{
					target = instructions.get(0);
				} else {
					LOG.error("Error in: {" + m.name + " | " + m.desc + "} No Instructions");
				}
				if(mPatch.opType == PatchOpType.Modify)
				{
					for (int index = 0; index < m.instructions.size() && !instructions.isEmpty(); index++ )
					{
						numInsertions = 0;
						while(target != null)
						{
							if(target.startLine == -1) {
								performDirectOperation(m.instructions, target);
								instructions.remove(0);
							} else if(this.isLineNumber(m.instructions.get(index), target.startLine)) {
								performAnchorOperation(m.instructions, target, index);
								instructions.remove(0);
							}
							else
							{
								break;
							}


							if (!instructions.isEmpty())
							{
								target = instructions.get(0);
							} 
							else 
							{
								target = null;
							}
						}
					}
				}
				else if(mPatch.opType == PatchOpType.Replace)
				{
					if (target != null && target.offset != -1)
					{
						for (int index = 0; index < m.instructions.size();)
						{
							if(index > target.offset)
								m.instructions.remove(m.instructions.get(index));
							else index++;
						}

					}
					for (int index = 0; index < m.instructions.size() && !instructions.isEmpty(); index++ )
					{
						numInsertions = 0;
						while(target != null)
						{
							if(target.startLine == -1) {
								performDirectOperation(m.instructions, target);
								instructions.remove(0);
							} else if(this.isLineNumber(m.instructions.get(index), target.startLine)) {
								performAnchorOperation(m.instructions, target, index);
								instructions.remove(0);
							}
							else
							{
								break;
							}


							if (!instructions.isEmpty())
							{
								target = instructions.get(0);
							} 
							else 
							{
								target = null;
							}
						}
						m.instructions.add(new InsnNode(Opcodes.RETURN));
					}
				}
				LOG.info("Inserted: " + classNode.name + " : {" + m.name + " | " + m.desc + "}");
			}
		}
		LOG.info("Attempting to Transform: " + classNode.name + " Complete");
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private int findLine(InsnList methodList, int line)
	{
		for (int index = 0; index < methodList.size(); index++)
		{
			if(this.isLineNumber(methodList.get(index), line))
			{
				return index;
			}
		}
		return -1;
	}

	private void performDirectOperation(InsnList methodInsn, InstrSet input)
	{
		AbstractInsnNode current = methodInsn.get(input.offset+numInsertions);
		switch(input.opType)
		{
		case InsertAfter:
			numInsertions+=input.iList.size();
			methodInsn.insert(current, input.iList);
			break;
		case InsertBefore:
			numInsertions+=input.iList.size();
			methodInsn.insertBefore(current, input.iList);
			break;
		case Remove:
			numInsertions--;
			methodInsn.remove(current);
			break;
		case Replace:
			if(current instanceof JumpInsnNode && input.iList.get(0) instanceof JumpInsnNode)
			{
				((JumpInsnNode)input.iList.get(0)).label = ((JumpInsnNode)current).label;
			}
			methodInsn.insert(current, input.iList);
			methodInsn.remove(current);
			break;
		default:
			break;
		}
	}

	private void performAnchorOperation(InsnList methodInsn, InstrSet input, int anchor)
	{
		AbstractInsnNode current = methodInsn.get(anchor + input.offset + numInsertions);
		if(input.iList.get(0) instanceof JumpInsnNode)
		{
			input.iList.set(input.iList.get(0), new JumpInsnNode(input.iList.get(0).getOpcode(), (LabelNode)current.getPrevious()));
		}
		switch(input.opType)
		{
		case InsertAfter:
			numInsertions+=input.iList.size();
			methodInsn.insert(current, input.iList);
			break;
		case InsertBefore:
			numInsertions+=input.iList.size();
			methodInsn.insertBefore(current, input.iList);
			break;
		case Remove:
			numInsertions--;
			methodInsn.remove(current);
			break;
		case Replace:
			methodInsn.insert(current, input.iList);
			methodInsn.remove(current);
			break;
		case Switch:
			/**
			 * Current is the first node. We will remove it and insert after the chosen index
			 * and then move back one, grab that node and move it to the original location
			 * */
			int otherAnchor = findLine(methodInsn, input.offsetLine);
			AbstractInsnNode other = methodInsn.get(otherAnchor + input.offsetSwitch + numInsertions);
			methodInsn.remove(current);
			methodInsn.insert(other, current);
			current = methodInsn.get(anchor + input.offset + numInsertions);
			methodInsn.remove(other);
			methodInsn.insertBefore(current, other);
			break;
		default:
			break;
		}
	}

	protected Map<String, Patch> getMethodNodeList()
	{
		if(NTMLoadingPlugin.runtimeDeobf)
		{
			return obfMethodNodes;
		}
		return mcpMethodNodes;
	}

	private boolean isLineNumber(AbstractInsnNode current, int line)
	{
		if(current instanceof LineNumberNode)
		{
			int l = ((LineNumberNode)current).line;
			if(l == line)
			{
				return true;
			}
		}
		return false;
	}

	public class InstrSet
	{
		/**
		 * InsnList of instructions that should be inserted at the specified point
		 */
		public InsnList iList;

		/**
		 * Insertion offset to from either the top of the file, or from the provided startLine
		 */
		public int offset;

		/**
		 * The line number of the LineNumberNode to use as the starting offset, also known as the anchor point. 
		 * If this is -1 then the top of the method is used as the anchor point
		 */
		public int startLine = -1;

		/**
		 * The type of operation that should be performed at the given offset
		 */
		public InstrOpType opType;


		public int offsetSwitch = -1;
		public int offsetLine = -1;

		public InstrSet(InsnList list, int off, InstrOpType op)
		{
			iList = list;
			offset = off;
			opType = op;
		}
		public InstrSet(AbstractInsnNode node, int off, InstrOpType op)
		{
			iList = new InsnList();
			iList.add(node);
			offset = off;
			opType = op;
		}
		public InstrSet(InsnList list, int startLineNum, int off, InstrOpType op)
		{
			iList = list;
			startLine = startLineNum;
			offset = off;
			opType = op;
		}
		public InstrSet(AbstractInsnNode node, int startLineNum, int off, InstrOpType op)
		{
			iList = new InsnList();
			iList.add(node);
			startLine = startLineNum;
			offset = off;
			opType = op;
		}
		public InstrSet(int startLineNum, int off, int swLineNum, int swOffset)
		{
			startLine = startLineNum;
			offset = off;
			opType = InstrOpType.Switch;
			offsetSwitch = swOffset;
			offsetLine = swLineNum;
		}
	}

	public class Patch
	{
		/**
		 * InstrSet Instance that should be used to modify the given method
		 */
		public List<InstrSet> instructions;
		public PatchOpType opType = PatchOpType.Modify;

		public Patch(List<InstrSet> set)
		{
			instructions = set;
		}

		public Patch(List<InstrSet> set, PatchOpType op)
		{
			instructions = set;
			opType = op;
		}
	}

	public enum PatchOpType
	{
		Modify,
		Replace,
	}

	public enum InstrOpType
	{
		InsertAfter,
		InsertBefore,
		Switch,
		Replace,
		Remove;
	}

	public class JumpNode extends JumpInsnNode
	{
		public int line;
		public JumpNode(int opcode, LabelNode label) {
			super(opcode, label);
		}

		public JumpNode(int opcode, int labelLine) 
		{
			super(opcode, null);
			line = labelLine;
		}

	}
}
