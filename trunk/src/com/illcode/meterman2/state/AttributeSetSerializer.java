package com.illcode.meterman2.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.illcode.meterman2.AttributeSet;

import java.util.BitSet;

public class AttributeSetSerializer extends Serializer<AttributeSet>
{
    public void write(Kryo kryo, Output output, AttributeSet attributeSet) {
        kryo.writeObject(output, attributeSet.asBitSet());
    }

    public AttributeSet read(Kryo kryo, Input input, Class<AttributeSet> type) {
        BitSet bits = kryo.readObject(input, BitSet.class);
        return AttributeSet.fromBitSet(bits);
    }
}
