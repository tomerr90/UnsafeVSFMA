package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sun.misc.Unsafe;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(
    jvmArgs = {
        "-XX:-TieredCompilation",
        "-Xms16g",
        "--enable-preview"
})
public class FMASerDeOffHeap {

    private static final Unsafe UNSAFE = getUnsafe();

    long bufferUnsafe;
    MemorySegment memSegment;

    @Benchmark
    public void unsafeWrite() {
        UNSAFE.putInt(bufferUnsafe, 0, 3);
        UNSAFE.putLong(bufferUnsafe, 4, 9999999999999999L);
        UNSAFE.putInt(bufferUnsafe, 12, 1234);
        UNSAFE.putLong(bufferUnsafe, 16, 1234567876543224L);
        UNSAFE.putDouble(bufferUnsafe, 24, 32.6875648);
        UNSAFE.putDouble(bufferUnsafe, 32, 12345.99933454);
        UNSAFE.putFloat(bufferUnsafe, 40, 77.855f);
    }

    @Benchmark
    public void unsafeRead(Blackhole blackhole) {
        blackhole.consume(UNSAFE.getInt(bufferUnsafe, 0));
        blackhole.consume(UNSAFE.getLong(bufferUnsafe, 4));
        blackhole.consume(UNSAFE.getInt(bufferUnsafe, 12));
        blackhole.consume(UNSAFE.getLong(bufferUnsafe, 16));
        blackhole.consume(UNSAFE.getDouble(bufferUnsafe, 24));
        blackhole.consume(UNSAFE.getDouble(bufferUnsafe, 32));
        blackhole.consume(UNSAFE.getFloat(bufferUnsafe, 40));
    }

    @Benchmark
    public void fmaWrite() {
        memSegment.set(ValueLayout.JAVA_INT_UNALIGNED, 0, 3);
        memSegment.set(ValueLayout.JAVA_LONG_UNALIGNED, 4, 9999999999999999L);
        memSegment.set(ValueLayout.JAVA_INT_UNALIGNED, 12, 1234);
        memSegment.set(ValueLayout.JAVA_LONG_UNALIGNED, 16, 1234567876543224L);
        memSegment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, 24, 32.6875648);
        memSegment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, 32, 12345.99933454);
        memSegment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, 40, 77.855f);
    }

    @Benchmark
    public void fmaRead(Blackhole blackhole) {
        blackhole.consume(memSegment.get(ValueLayout.JAVA_INT_UNALIGNED, 0));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_LONG_UNALIGNED, 4));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_INT_UNALIGNED, 12));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_LONG_UNALIGNED, 16));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, 24));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, 32));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, 40));
    }

    @Setup(Level.Trial)
    public void setup() {
        bufferUnsafe = UNSAFE.allocateMemory(100);
        memSegment = Arena.global().allocate(100);
    }

    @TearDown
    public void teardown() {
        UNSAFE.freeMemory(bufferUnsafe);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(FMASerDeOffHeap.class.getSimpleName())
            .build();

        new Runner(options).run();
    }

    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
