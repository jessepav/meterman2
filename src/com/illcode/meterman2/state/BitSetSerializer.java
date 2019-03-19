package com.illcode.meterman2.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.BitSet;

class BitSetSerializer extends Serializer<BitSet>
{
    // See SVN rev 142 for the code that used byte arrays rather than long arrays.

    public void write(Kryo kryo, Output output, BitSet bitSet) {
        long[] longs = bitSet.toLongArray();
        output.writeInt(longs.length, true);
        output.writeLongs(longs);
    }

    public BitSet read(Kryo kryo, Input input, Class<BitSet> type) {
        int len = input.readInt(true);
        return BitSet.valueOf(input.readLongs(len));
    }
}
