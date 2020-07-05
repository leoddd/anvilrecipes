function initializeCoreMod() {
    var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');

    var AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var IincInsnNode = Java.type('org.objectweb.asm.tree.IincInsnNode');
    var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var LineNumberNode = Java.type('org.objectweb.asm.tree.LineNumberNode');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

    return {
        'RepairContainer': {
            target: {
                'type': 'METHOD',
                'class': 'net.minecraft.inventory.container.RepairContainer',
                'methodName': 'func_82848_d',
                'methodDesc': '()V'
            },
            transformer: function(method)
            {
                var transforms = 0;
                var newInstructions = new InsnList();

                var backLabel = new LabelNode();
                var forwardLabel = new LabelNode();

                for (var i = 0; i < method.instructions.size(); ++i)
                {
                    var instruction = method.instructions.get(i);

                    if (transforms == 0 &&
                        instruction.getType() == AbstractInsnNode.LINE &&
                        instruction.line == 155)
                    {
                        var newInstructions = new InsnList();

                        newInstructions.add(new JumpInsnNode(Opcodes.GOTO, forwardLabel));
                        newInstructions.add(backLabel);

                        method.instructions.insert(instruction, newInstructions);

                        transforms = 1;
                    }
                    else if (transforms == 1 &&
                        instruction.getType() == AbstractInsnNode.LINE &&
                        instruction.line == 156)
                    {
                        method.instructions.insert(instruction, forwardLabel);

                        for (var j = i; j < method.instructions.size(); ++j)
                        {
                            var branchBack = method.instructions.get(j);

                            if (branchBack.getOpcode() == Opcodes.IFNE)
                            {
                                branchBack.label = backLabel;
                                break;
                            }
                        }

                        transforms = 2;
                        break;
                    }
                }

                if (transforms == 2) {
                    print("[extendedsmithing] Transformed RepairContainer");
                } else {
                    print("[extendedsmithing] Failed to transform RepairContainer");
                }

                return method;
            }
        }
    };
}