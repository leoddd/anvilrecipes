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
                var finishedTransforming = false;
                var transforms = 0;
                var newInstructions = new InsnList();

                var backLabel = new LabelNode();
                var forwardLabel = new LabelNode();
                var doneLabel = new LabelNode();

                for (var i = 0; i < method.instructions.size(); ++i)
                {
                    var instruction = method.instructions.get(i);

                    // Find the first IFNE, which is the one on line 155.
                    if (transforms == 0 &&
                        instruction.getOpcode() == Opcodes.IFNE)
                    {
                        var theAload = instruction.getPrevious().getPrevious()
                        if (theAload.getOpcode() == Opcodes.ALOAD) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new JumpInsnNode(Opcodes.GOTO, forwardLabel));
                            newInstructions.add(backLabel);

                            method.instructions.insertBefore(theAload, newInstructions);
                            i += newInstructions.size();

                            transforms = 1;
                        }
                    }

                    else if (transforms == 1 &&
                            instruction.getOpcode() == Opcodes.ALOAD)
                    {
                        var newInstructions = new InsnList();

                        newInstructions.add(new JumpInsnNode(Opcodes.GOTO, doneLabel));
                        newInstructions.add(forwardLabel);

                        method.instructions.insertBefore(instruction, newInstructions);
                        i += newInstructions.size();

                        transforms = 2;
                    }

                    else if (transforms == 2 &&
                            instruction.getOpcode() == Opcodes.IFNE)
                    {
                        instruction.label = backLabel;

                        transforms = 3;
                    }

                    else if (transforms == 3 &&
                            instruction.getType() == AbstractInsnNode.LABEL)
                    {
                        method.instructions.insert(instruction, doneLabel);
                        i++;

                        transforms = 4;
                    }

                    else if (transforms == 4)
                    {
                        finishedTransforming = true;
                        break;
                    }
                }

                if (finishedTransforming) {
                    print("[extendedsmithing] Transformed RepairContainer");
                } else {
                    print("[extendedsmithing] Failed to transform RepairContainer");
                }

                return method;
            }
        }
    };
}