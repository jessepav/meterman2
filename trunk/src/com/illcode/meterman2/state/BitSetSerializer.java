package com.illcode.meterman2.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.BitSet;

class BitSetSerializer extends Serializer<BitSet>
{
    public void write(Kryo kryo, Output output, BitSet bitSet) {
        final byte[] bytes = bitSet.toByteArray();
        output.writeInt(bytes.length, true);
        output.writeBytes(bytes);
    }

    public BitSet read(Kryo kryo, Input input, Class<BitSet> type) {
        final int len = input.readInt(true);
        return BitSet.valueOf(input.readBytes(len));
    }
}
