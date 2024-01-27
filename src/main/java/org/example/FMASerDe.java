package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sun.misc.Unsafe;

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
public class FMASerDe {

    private static final Unsafe UNSAFE = getUnsafe();
    private static final byte[] BUFFER = new byte[100];
    private static final long ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    @Benchmark
    public void unsafeWrite() {
        UNSAFE.putInt(BUFFER, ARRAY_OFFSET, 3);
        UNSAFE.putLong(BUFFER, ARRAY_OFFSET + 4, 9999999999999999L);
        UNSAFE.putInt(BUFFER, ARRAY_OFFSET + 12, 1234);
        UNSAFE.putLong(BUFFER, ARRAY_OFFSET + 16, 1234567876543224L);
        UNSAFE.putDouble(BUFFER, ARRAY_OFFSET + 24, 32.6875648);
        UNSAFE.putDouble(BUFFER, ARRAY_OFFSET + 32, 12345.99933454);
        UNSAFE.putFloat(BUFFER, ARRAY_OFFSET + 40, 77.855f);
    }

    @Benchmark
    public void unsafeRead(Blackhole blackhole) {
        blackhole.consume(UNSAFE.getInt(BUFFER, ARRAY_OFFSET));
        blackhole.consume(UNSAFE.getLong(BUFFER, ARRAY_OFFSET + 4));
        blackhole.consume(UNSAFE.getInt(BUFFER, ARRAY_OFFSET + 12));
        blackhole.consume(UNSAFE.getLong(BUFFER, ARRAY_OFFSET + 16));
        blackhole.consume(UNSAFE.getDouble(BUFFER, ARRAY_OFFSET + 24));
        blackhole.consume(UNSAFE.getDouble(BUFFER, ARRAY_OFFSET + 32));
        blackhole.consume(UNSAFE.getFloat(BUFFER, ARRAY_OFFSET + 40));
    }

    @Benchmark
    public void fmaWrite() {
        MemorySegment memSegment = MemorySegment.ofArray(BUFFER);

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
        MemorySegment memSegment = MemorySegment.ofArray(BUFFER);

        blackhole.consume(memSegment.get(ValueLayout.JAVA_INT_UNALIGNED, 0));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_LONG_UNALIGNED, 4));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_INT_UNALIGNED, 12));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_LONG_UNALIGNED, 16));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, 24));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, 32));
        blackhole.consume(memSegment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, 40));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(FMASerDe.class.getSimpleName())
            .exclude(FMASerDeOffHeap.class.getSimpleName())
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
