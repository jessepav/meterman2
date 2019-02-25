package com.illcode.meterman2.ui;

import static com.illcode.meterman2.MMLogging.logger;

import org.apache.commons.lang3.StringUtils;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.libraries.LibraryJOAL;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOrbis;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * Class for handling music (long streaming audio) and sounds (short buffered audio).
 * <p/>
 * Only one piece of music may be playing at a time, while multiple (up to the number of system sound channels)
 * sounds can be played simultaneously.
 */
public class SoundManager
{
    private boolean initialized;
    private boolean musicEnabled, soundEnabled;

    private SoundSystem soundSystem;

    /** Sourcenames of all the non-streaming sounds currently loaded. */
    private HashSet<String> loadedSounds;

    /** Sourcenames of all the sources currently loaded. */
    private HashSet<String> loadedSources;

    /** Source name of the music currently playing (null if no music is playing). */
    private String musicSource;

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

        loadedSounds = new HashSet<>();
        loadedSources = new HashSet<>();
        musicSource = null;

        initialized = true;
    }

    /**
     * Dispose of resources allocated during initialization, and stop any extra threads.
     */
    public void dispose() {
        if (!initialized)
            return;

        clearAudio();
        soundSystem.cleanup();

        initialized = false;
    }

    /**
     * Unload all sound and music.
     */
    void clearAudio() {
        stopMusic();
        for (String sourcename : loadedSources)
            soundSystem.removeSource(sourcename);
        loadedSources.clear();
        for(String sourcename : loadedSounds)
            soundSystem.unloadSound(sourcename);
        loadedSounds.clear();
    }

    /**
     * Set a global volume multiplier.
     * @param volume global volume multiplier (nominal is 1.0)
     */
    void setGlobalVolume(float volume) {
        soundSystem.setMasterVolume(volume);
    }

    /**
     * Enables or disables the playing of music
     * @param enabled true if music should be enabled
     */
    void setMusicEnabled(boolean enabled) {
        if (musicEnabled != enabled) {
            musicEnabled = enabled;
            if (!musicEnabled)
                stopMusic();
        }
    }

    /** Returns true if the playing of music is enabled.*/
    boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Loads music from a file
     * @param name the name by which the music will be referred by this SoundManager. This name
     *      should have an extension (ex. ".ogg") indicating the type of audio data that will be loaded.
     * @param p the Path to load music from
     */
    void loadMusic(String name, Path p) {
        if (loadedSources.contains(name))
            return;
        try {
            soundSystem.newStreamingSource(false, name, p.toUri().toURL(), name,
                true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
            loadedSources.add(name);
        } catch (MalformedURLException e) {
            logger.warning("SoundManager: Malformed URL for Path " + p.toString());
        }
    }

    /**
     * Plays music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     *
     */
    void playMusic(String name, boolean loop) {
        if (musicEnabled) {
            musicSource = name;
            soundSystem.setLooping(musicSource, loop);
            soundSystem.play(musicSource);
        }
    }

    /**
     * Stops playback for the currently playing music, if any.
     */
    void stopMusic() {
        if (musicSource != null) {
            soundSystem.stop(musicSource);
            musicSource = null;
        }
    }

    /** Pauses any music currently playing. */
    void pauseMusic() {
        if (musicSource != null)
            soundSystem.pause(musicSource);
    }

    /** Resumes playing the current music piece.. */
    void resumeMusic() {
        if (musicEnabled && musicSource != null)
            soundSystem.play(musicSource);
    }

    /**
     * Unloads Music previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadMusic(String name) {
        if (musicSource != null && musicSource.equals(name))
            stopMusic();
        if (loadedSources.remove(name))
            soundSystem.removeSource(name);
    }

    /**
     * Enables or disables the playing of sounds
     * @param enabled true if sounds should be enabled
     */
    void setSoundEnabled(boolean enabled) {
        if (soundEnabled != enabled)
            soundEnabled = enabled;
    }

    /** Returns true if the playing of sounds is enabled.*/
    boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Loads a Sound from a file
     * @param name the name by which the sound will be referred by this SoundManager. This name
     *      should have an extension (ex. ".wav") indicating the type of audio data that will be loaded.
     * @param p the Path to load sound from
     */
    void loadSound(String name, Path p) {
    }

    /**
     * Plays a sound previously loaded.
     * <p/>
     * @param name name of the sound, as specified in {@link #loadSound}
     */
    void playSound(String name) {
    }

    /**
     * Unloads a Sound previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadSound(String name) {
    }
}
