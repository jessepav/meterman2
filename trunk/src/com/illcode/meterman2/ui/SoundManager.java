package com.illcode.meterman2.ui;

import static com.illcode.meterman2.MMLogging.logger;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.libraries.LibraryJOAL;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOrbis;

import java.nio.file.Path;
import java.util.logging.Level;

public class SoundManager
{
    private boolean initialized;
    private boolean musicEnabled, soundEnabled;

    private SoundSystem soundSystem;

    /**
     * Initialize the sound manager, starting any threads it may use.
     */
    public void init() {
        if (initialized)
            return;

        Class<?> libraryType;
        if (SoundSystem.libraryCompatible(LibraryJOAL.class))
            libraryType = LibraryJOAL.class;
        else if (SoundSystem.libraryCompatible(LibraryJavaSound.class))
            libraryType = LibraryJavaSound.class;
        else
            libraryType = Library.class;

        try {
            SoundSystemConfig.setCodec("wav", CodecWav.class);
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
            soundSystem = new SoundSystem(libraryType);
            logger.info("SoundManager using SoundSystem class " + libraryType.getName());
        } catch (SoundSystemException ex) {
            logger.log(Level.WARNING, "SoundManager.init()", ex);
        }

        initialized = true;
    }

    /**
     * Dispose of resources allocated during initialization, and stop any extra threads.
     */
    public void dispose() {
        if (!initialized)
            return;

        soundSystem.cleanup();

        initialized = false;
    }

    /**
     * Unload all sound and music.
     */
    void clearAudio() {
    }

    /**
     * Set a global volume multiplier.
     * @param volume global volume multiplier (nominal is 1.0)
     */
    void setGlobalVolume(double volume) {
    }

    /**
     * Enables or disables the playing of music
     * @param enabled true if music should be enabled
     */
    void setMusicEnabled(boolean enabled) {
    }

    /** Returns true if the playing of music is enabled.*/
    boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Loads music from a file
     * @param name the name by which the music will be referred by this SoundManager
     * @param p the Path to load music from
     */
    void loadMusic(String name, Path p) {
    }

    /**
     * Plays music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     * @param volume the relative volume at which to play the music (1.0 is nominal)
     */
    void playMusic(String name, boolean loop, double volume) {
    }

    /**
     * Stops playback for music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     */
    void stopMusic(String name) {
    }

    /** Pauses all music currently playing. */
    void pauseAllMusic() {
    }

    /** Resumes playing all music that was previously paused by a call to {@link #pauseAllMusic()}. */
    void resumeAllMusic() {
    }

    /**
     * Enables or disables the playing of sounds
     * @param enabled true if sounds should be enabled
     */
    void setSoundEnabled(boolean enabled) {
    }

    /** Returns true if the playing of sounds is enabled.*/
    boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Unloads Music previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadMusic(String name) {
    }

    /**
     * Loads a Sound from a file
     * @param name the name by which the sound will be referred by this SoundManager
     * @param p the Path to load sound from
     */
    void loadSound(String name, Path p) {
    }

    /**
     * Plays a sound previously loaded.
     * <p/>
     * @param name name of the sound, as specified in {@link #loadSound}
     * @param volume the relative volume at which to play the sound (1.0 is nominal)
     */
    void playSound(String name, double volume) {
    }

    /**
     * Unloads a Sound previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadSound(String name) {
    }
}
