package com.illcode.meterman2.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.illcode.meterman2.AttributeSet;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Handles serialization and deserialization of game state.
 */
public final class KryoPersistence
{
    private Kryo kryo;

    public KryoPersistence() {
        kryo = new Kryo();

        // My serializers
        kryo.register(BitSet.class, new BitSetSerializer());
        kryo.register(AttributeSet.class, new AttributeSetSerializer());

        // Serializers from kryo-serializers
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());

        // properly serialize multiple references and cyclic graphs
        kryo.setReferences(true);
    }

    /** Dispose of resources allocated during construction. */
    public void dispose() {
        kryo = null;
    }

    /** Write serialized game state to an output stream. */
    public void saveGameState(GameState state, OutputStream out) {
        Output output = new Output(out);
        kryo.writeObject(output, state);
        output.flush();
    }

    /** Reconstruct a game state object from serialized data on an input stream. */
    public GameState loadGameState(InputStream in) {
        Input input = new Input(in);
        GameState state = kryo.readObject(input, GameState.class);
        return state;
    }
}
