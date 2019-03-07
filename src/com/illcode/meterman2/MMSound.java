package com.illcode.meterman2;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJOAL;
import paulscode.sound.libraries.LibraryJavaSound;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Class for handling music (long streaming audio) and sounds (short buffered audio).
 * <p/>
 * Only one piece of music may be playing at a time, while multiple (up to the number of system sound channels)
 * sounds can be played simultaneously.
 */
public class MMSound
{
    private boolean musicEnabled, soundEnabled;

    private SoundSystem soundSystem;

    /** Sourcenames of all the non-streaming sounds currently loaded. */
    private HashSet<String> loadedSounds;

    /** Sourcenames of all the sources currently loaded. */
    private HashSet<String> loadedSources;

    /** Source name of the music currently playing (null if no music is playing). */
    private String musicSource;

    /**
     * Create an instance of the sound manager, starting any threads it may use.
     */
    MMSound() {
        Class<?> libraryType;
        String soundPref = Utils.getPref("sound-library");
        if (soundPref == null) {  // we do auto-selection
            if (SoundSystem.libraryCompatible(LibraryJOAL.class))
                libraryType = LibraryJOAL.class;
            else if (SoundSystem.libraryCompatible(LibraryJavaSound.class))
                libraryType = LibraryJavaSound.class;
            else
                libraryType = Library.class;
        } else if (soundPref.equals("joal")) {
            libraryType = LibraryJOAL.class;
        } else if (soundPref.equals("javasound")) {
            libraryType = LibraryJavaSound.class;
        } else {
            libraryType = Library.class;
        }

        try {
            SoundSystemConfig.setCodec("wav", CodecWav.class);
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
            soundSystem = new SoundSystem(libraryType);
            logger.info("MMSound using SoundSystem class " + libraryType.getName());
        } catch (SoundSystemException ex) {
            logger.log(Level.WARNING, "MMSound.init()", ex);
        }

        loadedSounds = new HashSet<>();
        loadedSources = new HashSet<>();
        musicSource = null;
    }

    /**
     * Dispose of resources allocated during initialization, and stop any extra threads.
     */
    void dispose() {
        clearAudio();
        soundSystem.cleanup();
    }

    /**
     * Unload all sound and music.
     */
    public void clearAudio() {
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
    public void setGlobalVolume(float volume) {
        soundSystem.setMasterVolume(volume);
    }

    /**
     * Enables or disables the playing of music
     * @param enabled true if music should be enabled
     */
    public void setMusicEnabled(boolean enabled) {
        if (musicEnabled != enabled) {
            musicEnabled = enabled;
            if (!musicEnabled)
                stopMusic();
        }
    }

    /** Returns true if the playing of music is enabled.*/
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Loads music from a file
     * @param name the name by which the music will be referred by this MMSound. This name
     *      should have an extension (ex. ".ogg") indicating the type of audio data that will be loaded.
     * @param p the Path to load music from
     */
    public void loadMusic(String name, Path p) {
        if (loadedSources.contains(name))
            return;
        try {
            soundSystem.newStreamingSource(false, name, p.toUri().toURL(), name,
                true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
            loadedSources.add(name);
        } catch (MalformedURLException e) {
            logger.warning("MMSound: Malformed URL for Path " + p.toString());
        }
    }

    /**
     * Plays music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     *
     */
    public void playMusic(String name, boolean loop) {
        if (musicEnabled) {
            stopMusic();
            musicSource = name;
            soundSystem.setLooping(musicSource, loop);
            soundSystem.play(musicSource);
        }
    }

    /**
     * Stops playback for the currently playing music, if any.
     */
    public void stopMusic() {
        if (musicSource != null) {
            soundSystem.stop(musicSource);
            musicSource = null;
        }
    }

    /** Pauses any music currently playing. */
    public void pauseMusic() {
        if (musicSource != null)
            soundSystem.pause(musicSource);
    }

    /** Resumes playing the current music piece.. */
    public void resumeMusic() {
        if (musicEnabled && musicSource != null)
            soundSystem.play(musicSource);
    }

    /**
     * Unloads Music previously loaded by this MMSound
     * @param name the name under which the audio was loaded
     */
    public void unloadMusic(String name) {
        if (musicSource != null && musicSource.equals(name))
            stopMusic();
        if (loadedSources.remove(name))
            soundSystem.removeSource(name);
    }

    /**
     * Enables or disables the playing of sounds
     * @param enabled true if sounds should be enabled
     */
    public void setSoundEnabled(boolean enabled) {
        if (soundEnabled != enabled)
            soundEnabled = enabled;
    }

    /** Returns true if the playing of sounds is enabled.*/
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Loads a Sound from a file
     * @param name the name by which the sound will be referred by this MMSound. This name
     *      should have an extension (ex. ".wav") indicating the type of audio data that will be loaded.
     * @param p the Path to load sound from
     */
    public void loadSound(String name, Path p) {
        if (!loadedSounds.contains(name)) {
            try {
                URL url = p.toUri().toURL();
                soundSystem.loadSound(url, name);
                loadedSounds.add(name);
                soundSystem.newSource(false, name, url, name,
                    false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
                loadedSources.add(name);
            } catch (MalformedURLException e) {
                logger.warning("MMSound: Malformed URL for Path " + p.toString());
            }
        }
    }

    /**
     * Plays a sound previously loaded.
     * <p/>
     * @param name name of the sound, as specified in {@link #loadSound}
     */
    public void playSound(String name) {
        if (soundEnabled)
            soundSystem.play(name);
    }

    /**
     * Unloads a Sound previously loaded by this MMSound
     * @param name the name under which the sound was loaded
     */
    public void unloadSound(String name) {
        if (loadedSounds.remove(name)) {
            soundSystem.unloadSound(name);
            soundSystem.removeSource(name);
            loadedSources.remove(name);
        }
    }
}