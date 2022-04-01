package com.cleanroommc.hadenoughids.core.visitors;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;

public class ItemStackVisitor extends ClassVisitor implements Opcodes {

    public static final String CLASS_NAME = "net.minecraft.item.ItemStack";

    public static final String DELEGATED_INIT_METHOD_DESC = "(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V";
    public static final String NBT_INIT_METHOD_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;)V";
    public static final String IS_EMPTY_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "isEmpty" : "func_190926_b";

    private static final String ITEM_FIELD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "item" : "field_151002_e";
    private static final String ITEM_DAMAGE_FIELD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "itemDamage" : "field_77991_e";


    public ItemStackVisitor(ClassWriter classWriter) {
        super(ASM5, classWriter);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            if (desc.equals(DELEGATED_INIT_METHOD_DESC)) {
                return new DelegatedInitMethodVisitor(visitor);
            } else if (desc.equals(NBT_INIT_METHOD_DESC)) {
                return new NBTInitMethodVisitor(visitor);
            }
        } else if (name.equals(IS_EMPTY_METHOD)) {
            return new IsEmptyMethodVisitor(visitor);
        }
        return visitor;
    }

    private static class DelegatedInitMethodVisitor extends MethodVisitor {

        private static final String STACK_SIZE_FIELD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "stackSize" : "field_77994_a";

        private boolean removeItemDamageChecks = false;
        private int itemDamagePutFieldInsnCount = 0;

        private DelegatedInitMethodVisitor(MethodVisitor methodVisitor) {
            super(ASM5, methodVisitor);
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (removeItemDamageChecks) {
                if (ITEM_DAMAGE_FIELD.equals(name)) {
                    if (++itemDamagePutFieldInsnCount == 2) {
                        removeItemDamageChecks = false;
                    }
                }
                return;
            }
            if (ITEM_DAMAGE_FIELD.equals(name)) {
                super.visitVarInsn(ALOAD, 1);
                super.visitMethodInsn(
                        INVOKESTATIC,
                        "com/cleanroommc/hadenoughids/core/hooks/UniversalHooks",
                        "getCorrectItemMetadata",
                        "(ILnet/minecraft/item/Item;)I",
                        false);
            } else if (STACK_SIZE_FIELD.equals(name)) {
                removeItemDamageChecks = true;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitLabel(Label label) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitLabel(label);
        }

        @Override
        public void visitInsn(int opcode) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitJumpInsn(opcode, label);
        }

    }

    private static class NBTInitMethodVisitor extends MethodVisitor {

        private boolean removeItemDamageChecks = false;

        private NBTInitMethodVisitor(MethodVisitor methodVisitor) {
            super(ASM5, methodVisitor);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (removeItemDamageChecks) {
                removeItemDamageChecks = false;
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, owner, ITEM_FIELD, "Lnet/minecraft/item/Item;");
                super.visitVarInsn(ALOAD, 1);
                super.visitMethodInsn(
                        INVOKESTATIC,
                        "com/cleanroommc/hadenoughids/core/hooks/UniversalHooks",
                        "getCorrectItemMetadataFromNBT",
                        "(Lnet/minecraft/item/Item;Lnet/minecraft/nbt/NBTTagCompound;)I",
                        false);
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == ICONST_0) {
                removeItemDamageChecks = true;
                return;
            } else if (removeItemDamageChecks) {
                return;
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (removeItemDamageChecks) {
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

    }

    private static class IsEmptyMethodVisitor extends MethodVisitor {

        private boolean wipeItemDamageTernaryChecks = false;

        private IsEmptyMethodVisitor(MethodVisitor methodVisitor) {
            super(ASM5, methodVisitor);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            if (ITEM_DAMAGE_FIELD.equals(name)) {
                wipeItemDamageTernaryChecks = true;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitInsn(int opcode) {
            if (wipeItemDamageTernaryChecks) {
                if (opcode != IRETURN) {
                    return;
                }
                wipeItemDamageTernaryChecks = false;
                // super.visitFrame(F_SAME1, 0, new Object[] { }, 1, new Object[] { INTEGER });
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "net/minecraft/item/ItemStack", ITEM_FIELD, "Lnet/minecraft/item/Item;");
                super.visitMethodInsn(
                        INVOKESTATIC,
                        "com/cleanroommc/hadenoughids/core/hooks/UniversalHooks",
                        "getMetadataSignifyEmpty",
                        "(ILnet/minecraft/item/Item;)Z",
                        false);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitLabel(Label label) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitLabel(label);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            if (wipeItemDamageTernaryChecks) {
                return;
            }
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

    }

}
