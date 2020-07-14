function initializeCoreMod() {
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');

    var AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
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

            transformer: function(method)
            {
                var transforms = 0;
                var newInstructions = new InsnList();

                var backLabel = new LabelNode();
                var forwardLabel = new LabelNode();
                var doneLabel = new LabelNode();

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
                        var newInstructions = new InsnList();

                        newInstructions.add(new JumpInsnNode(Opcodes.GOTO, doneLabel));
                        newInstructions.add(forwardLabel);

                        method.instructions.insert(instruction, newInstructions);

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
                    }
                    else if (transforms == 2 &&
                        instruction.getType() == AbstractInsnNode.LINE &&
                        instruction.line == 157)
                    {
                        method.instructions.insert(instruction, doneLabel);

                        transforms = 3;
                        break;
                    }
                }

                if (transforms == 3) {
                    print("[extendedsmithing] Transformed RepairContainer");
                } else {
                    print("[extendedsmithing] Failed to transform RepairContainer");
                }

                return method;
            }
        }
    };
}