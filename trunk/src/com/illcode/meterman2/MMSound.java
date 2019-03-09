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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Class for handling music (long streaming audio) and sounds (short buffered audio).
 * <p/>
 * Only one piece of music may be playing at a time, while multiple (up to the number of system sound channels)
 * sounds can be played simultaneously.
 * <p/>
 * Clients will need to add source mappings by calling {@link #addSourceMaping(String, Path, boolean)} before audio
 * can be played.
 */
public final class MMSound
{
    private boolean musicEnabled, soundEnabled;

    private SoundSystem soundSystem;

    /** Map from source name to its associated record. */
    private Map<String,SoundRecord> sourceMap;

    // In the future we may change this to an LRU cache data structure.
    /** Source names of all the sources currently loaded. */
    private Set<String> loadedSources;

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

        sourceMap = new HashMap<>(32);
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
     * Add an audio source mapping. You must call {@link #loadSource(String)} before this source can be played.
     * @param name source name by which this audio will be referenced
     * @param path path to the audio file
     * @param isMusic true if the audio is music, that is, if it will be streamed rather than preloaded.
     */
    public void addSourceMaping(String name, Path path, boolean isMusic) {
        sourceMap.put(name, new SoundRecord(path, isMusic));
    }

    /**
     * Remove an audio source mapping. If the source was loaded, it will be unloaded.
     * @param name source name under which the audio was added.
     */
    public void removeSourceMapping(String name) {
        unloadSource(name);
        sourceMap.remove(name);
    }

    /**
     * Load a source into our system, allocating resources.
     * If the source is already loaded, this method does nothing.
     * @param name source name, as given in {@link #addSourceMaping(String, Path, boolean)}.
     */
    public void loadSource(String name) {
        if (!loadedSources.contains(name)) {
            SoundRecord rec = sourceMap.get(name);
            if (rec == null)
                return;
            final URL url = rec.getUrl();
            final String filename = rec.getFilename();
            if (rec.isMusic) {
                soundSystem.newStreamingSource(false, name, url, filename,
                    true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
            } else {
                soundSystem.newSource(false, name, url, filename,
                    false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
            }
            loadedSources.add(name);
        }
    }

    /**
     * Unload a source, freeing any resources it used.
     * @param name source name
     */
    public void unloadSource(String name) {
        if (loadedSources.remove(name)) {
            if (musicSource != null && musicSource.equals(name))
                stopMusic();
            removeLoadedSource(name);
        }
    }

    /**
     * Unload all audio and remove all sources.
     */
    public void clearAudio() {
        stopMusic();
        for (String name : loadedSources)
            removeLoadedSource(name);
        loadedSources.clear();
        sourceMap.clear();
    }

    // For internal use - does not remove 'name' from 'loadedSources'.
    private void removeLoadedSource(String name) {
        soundSystem.removeSource(name);
        SoundRecord record = sourceMap.get(name);
        if (!record.isMusic)
            soundSystem.unloadSound(record.getFilename());
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
     * Plays music.
     * @param name source name of the music
     * @param loop true if we should loop the music
     *
     */
    public void playMusic(String name, boolean loop) {
        if (musicEnabled) {
            stopMusic();
            loadSource(name);
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
     * Plays a sound.
     * @param name source name of the sound
     */
    public void playSound(String name) {
        if (soundEnabled) {
            loadSource(name);
            soundSystem.play(name);
        }
    }

    // Used as entries in the {@code sourceMap}.
    static final class SoundRecord
    {
        final Path path;
        final boolean isMusic;

        SoundRecord(Path path, boolean isMusic) {
            this.path = path;
            this.isMusic = isMusic;
        }

        /** Get the filename/identifier as passed to SoundSystem. */
        String getFilename() {
            return path.toString();
        }

        /** Get the URL of the path, or null if the URL is malformed. */
        URL getUrl() {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                logger.warning("MMSound: Malformed URL for Path: " + path.toString());
                return null;
            }
        }
    }
}
