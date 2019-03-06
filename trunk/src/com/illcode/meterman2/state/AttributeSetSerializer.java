package com.illcode.meterman2.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.illcode.meterman2.AttributeSet;

public class AttributeSetSerializer extends Serializer<AttributeSet>
{
    public void write(Kryo kryo, Output output, AttributeSet attributeSet) {
        final byte[] bytes = attributeSet.toByteArray();
        output.writeInt(bytes.length, true);
        output.writeBytes(bytes);
    }

    public AttributeSet read(Kryo kryo, Input input, Class<AttributeSet> type) {
        final int len = input.readInt(true);
        return AttributeSet.fromByteArray(input.readBytes(len));
    }
}
