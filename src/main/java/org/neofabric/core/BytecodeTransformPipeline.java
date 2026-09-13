package org.neofabric.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Ordered bytecode transformation chain applied before game/mod classes are defined. */
public final class BytecodeTransformPipeline {
    @FunctionalInterface
    public interface Transformer {
        byte[] transform(String className, byte[] bytecode);
    }

    private final CopyOnWriteArrayList<Transformer> transformers = new CopyOnWriteArrayList<>();

    public void add(Transformer transformer) {
        transformers.add(transformer);
    }

    public byte[] apply(String className, byte[] bytecode) {
        byte[] transformed = bytecode;
        for (Transformer transformer : transformers) {
            byte[] result = transformer.transform(className, transformed);
            if (result == null) throw new IllegalStateException("Transformer returned null for " + className);
            transformed = result;
        }
        return transformed;
    }

    public List<Transformer> transformers() {
        return List.copyOf(transformers);
    }
}
