package fluke.worleycaves.util;

import fluke.worleycaves.Main;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.BitSet;

public class CarverWorldAccess {

    private static final ThreadLocal<CarverWorldAccess> CONTEXT = new ThreadLocal<>();

    private final ChunkPrimer chunkPrimer;
    private final WeakReference<ServerWorld> world;

    public CarverWorldAccess(ServerWorld world, ChunkPrimer chunkPrimer) {
        this.world = new WeakReference<>(world);
        this.chunkPrimer = chunkPrimer;
    }

    public ServerWorld getWorld() {
        return world.get();
    }

    public BitSet getMask(GenerationStage.Carving stage) {
        return chunkPrimer.getOrCreateCarvingMask(stage);
    }

    /**
     * Consume the currently held CarvingContext.
     * A null value means we are in the wrong generation stage, or the context has already been consumed.
     */
    @Nullable
    public static CarverWorldAccess pop() {
        CarverWorldAccess context = CONTEXT.get();
        CONTEXT.set(null);
        return context;
    }

    /**
     * Peek the currently held CarvingContext without consuming it.
     * A null value means we are in the wrong generation stage, or the context has already been consumed.
     */
    @Nullable
    public static CarverWorldAccess peek() {
        return CONTEXT.get();
    }

    /**
     * Should only be called during the air carving stage (ChunkStatus.CARVERS).
     */
    public static void push(ServerWorld world, IChunk chunk) {
        if (chunk instanceof ChunkPrimer) {
            CONTEXT.set(new CarverWorldAccess(world, (ChunkPrimer) chunk));
        } else if (chunk != null) {
            // Shouldn't ever happen unless another mod has done a similar hook in ChunkStatus and changed the IChunk type
            Main.LOGGER.error("ERROR: Attempted to push invalid IChunk implementation to CarvingContext: {}", chunk.getClass());
        }
    }
}