function initializeCoreMod() {
    var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var IincInsnNode = Java.type('org.objectweb.asm.tree.IincInsnNode');
    var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

    return {
        'RepairContainer': {
            target: {
                'type': 'METHOD',
                'class': 'net.minecraft.inventory.container.RepairContainer',
                'methodName': 'func_82848_d',
                'methodDesc': '()V'
            },
            transformer: function(method) {
                var transformed = false;
                var arrayLength = method.instructions.size();
                var newInstructions = new InsnList();
                for (var i = 0; i < arrayLength; ++i) {
                    var instruction = method.instructions.get(i);

                    if (instruction.getOpcode() == Opcodes.IFNE) {
                        print(instruction.label.getLabel().info);
                        method.instructions.remove(instruction.getPrevious().getPrevious());
                        method.instructions.remove(instruction.getPrevious());
                        method.instructions.remove(instruction);

                        transformed = true;
                        break;
                    }
                }

                if (transformed) {
                    print("[extendedsmithing] Transformed RepairContainer");
                } else {
                    print("[extendedsmithing] Failed to transform RepairContainer");
                }

                return method;
            }
        }
    };
}