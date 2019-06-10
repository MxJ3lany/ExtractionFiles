package org.cf.smalivm;

import java.io.File;
import java.io.IOException;

import org.cf.smalivm.type.ClassManager;
import org.cf.smalivm.type.ClassManagerFactory;
import org.cf.smalivm.dex.SmaliParser;

public class VirtualMachineFactory {

    private static final int DEFAULT_MAX_ADDRESS_VISITS = 500;
    private static final int DEFAULT_MAX_CALL_DEPTH = 20;
    private static final int DEFAULT_MAX_EXECUTION_TIME = 5 * 60;
    private static final int DEFAULT_MAX_METHOD_VISITS = 1_000_000;

    public VirtualMachine build(ClassManager manager) {
        return new VirtualMachine(manager, DEFAULT_MAX_ADDRESS_VISITS, DEFAULT_MAX_CALL_DEPTH,
                        DEFAULT_MAX_METHOD_VISITS, DEFAULT_MAX_EXECUTION_TIME);
    }

    public VirtualMachine build(ClassManager manager, int maxAddressVisits, int maxCallDepth, int maxMethodVisits,
                    int maxExecutionTime) {
        return new VirtualMachine(manager, maxAddressVisits, maxCallDepth, maxMethodVisits, maxExecutionTime);
    }

    public VirtualMachine build(File smaliPath, int outputAPILevel, int maxAddressVisits, int maxCallDepth,
                    int maxMethodVisits, int maxExecutionTime) throws IOException {
        ClassManagerFactory factory = new ClassManagerFactory();
        ClassManager manager = factory.build(smaliPath, outputAPILevel);

        return new VirtualMachine(manager, maxAddressVisits, maxCallDepth, maxMethodVisits, maxExecutionTime);
    }

    public VirtualMachine build(String smaliPath) throws IOException {
        return build(smaliPath, SmaliParser.DEFAULT_API_LEVEL);
    }

    public VirtualMachine build(String smaliPath, int outputAPILevel) throws IOException {
        return build(smaliPath, outputAPILevel, DEFAULT_MAX_ADDRESS_VISITS, DEFAULT_MAX_CALL_DEPTH,
                        DEFAULT_MAX_METHOD_VISITS, DEFAULT_MAX_EXECUTION_TIME);
    }

    public VirtualMachine build(String smaliPath, int outputAPILevel, int maxAddressVisits, int maxCallDepth,
                    int maxMethodVisits, int maxExecutionTime) throws IOException {
        return build(new File(smaliPath), outputAPILevel, maxAddressVisits, maxCallDepth, maxMethodVisits,
                        maxExecutionTime);
    }
}
