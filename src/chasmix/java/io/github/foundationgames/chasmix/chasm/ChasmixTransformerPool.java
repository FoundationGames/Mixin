package io.github.foundationgames.chasmix.chasm;

import org.quiltmc.chasm.api.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChasmixTransformerPool {
    private static List<Transformer> pool;

    public static Optional<List<Transformer>> getCurrentPool() {
        return Optional.ofNullable(pool);
    }

    public static void createPool() {
        pool = new ArrayList<>();
    }

    public static void destroyPool() {
        pool = null;
    }

    static {
        createPool();
    }
}
