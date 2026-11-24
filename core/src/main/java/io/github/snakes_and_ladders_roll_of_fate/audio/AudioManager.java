package io.github.snakes_and_ladders_roll_of_fate.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized audio management system for Snakes & Ladders: Roll of Fate.
 * Manages streaming background music with smooth cross-fading and
 * low-latency sound effect playback with dynamic pitch randomization and volume persistence.
 */
public class AudioManager {
    private static final String PREF_NAME = "snakes_ladders_audio_prefs";
    private static final String PREF_MUSIC = "music_volume";
    private static final String PREF_SFX = "sfx_volume";

    private static final String[] SUPPORTED_MUSIC_EXTENSIONS = { ".ogg", ".mp3", ".wav" };
    private static final String[] MUSIC_SEARCH_DIRS = {
        "audio/music/",
        "assets/audio/music/",
        "music/",
        "assets/music/"
    };

    public enum MusicTrack {
        MENU("music_menu", "menu", "main_menu", "music_main_menu", "mainmenu", "music_mainmenu"),
        ACT1("music_act1", "act1", "act_1", "music_act_1"),
        ACT2("music_act2", "act2", "act_2", "music_act_2"),
        ACT3("music_act3", "act3", "act_3", "music_act_3"),
        BATTLE("music_battle", "battle", "combat", "music_combat"),
        VICTORY("music_victory", "victory", "win", "music_win"),
        DEFEAT("music_defeat", "defeat", "gameover", "music_gameover", "loss");

        private final String[] aliases;
        MusicTrack(String... aliases) {
            this.aliases = aliases;
        }
        public String[] getAliases() {
            return aliases;
        }
        public String getFilePath() {
            return "audio/music/" + aliases[0] + ".ogg";
        }
    }

    public enum SoundEffect {
        CLICK("audio/sfx/sfx_click.ogg"),
        DICE_ROLL("audio/sfx/sfx_dice_roll.ogg"),
        STEP("audio/sfx/sfx_step.ogg"),
        FOG_REVEAL("audio/sfx/sfx_fog_reveal.ogg"),
        LADDER_WINDUP("audio/sfx/sfx_ladder_windup.ogg"),
        LADDER_SOAR("audio/sfx/sfx_ladder_soar.ogg"),
        LADDER_LAND("audio/sfx/sfx_ladder_land.ogg"),
        SNAKE_HISS("audio/sfx/sfx_snake_hiss.ogg"),
        SNAKE_SLIDE("audio/sfx/sfx_snake_slide.ogg"),
        POTION_DRINK("audio/sfx/sfx_potion.ogg"),
        ITEM_EQUIP("audio/sfx/sfx_equip.ogg"),
        SCOUT_FLARE("audio/sfx/sfx_scout.ogg"),
        ATTACK_SLASH("audio/sfx/sfx_attack.ogg"),
        DEFEND_GUARD("audio/sfx/sfx_defend.ogg"),
        MONSTER_HIT("audio/sfx/sfx_hit_monster.ogg"),
        PLAYER_HURT("audio/sfx/sfx_hit_player.ogg"),
        SANCTUARY_HEAL("audio/sfx/sfx_sanctuary.ogg"),
        LEVEL_UP("audio/sfx/sfx_level_up.ogg");

        private final String filePath;
        SoundEffect(String filePath) {
            this.filePath = filePath;
        }
        public String getFilePath() {
            return filePath;
        }
    }

    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;

    private Music currentMusic;
    private MusicTrack currentTrack;

    private Music nextMusic;
    private MusicTrack nextTrack;
    private boolean nextLoop = true;

    private boolean isCrossFading = false;
    private float fadeElapsed = 0f;
    private static final float FADE_DURATION = 0.45f;

    private final Map<SoundEffect, Sound> soundCache = new EnumMap<>(SoundEffect.class);
    private boolean initialized = false;

    public void init() {
        if (initialized) return;

        try {
            Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
            musicVolume = prefs.getFloat(PREF_MUSIC, 0.7f);
            sfxVolume = prefs.getFloat(PREF_SFX, 0.8f);

            // Preload common sound effects to eliminate frame-stutter on first use
            for (SoundEffect sfx : SoundEffect.values()) {
                loadSound(sfx);
            }
            initialized = true;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error during audio initialization: " + e.getMessage());
        }
    }

    private Sound loadSound(SoundEffect sfx) {
        if (soundCache.containsKey(sfx)) {
            return soundCache.get(sfx);
        }
        try {
            if (Gdx.files.internal(sfx.getFilePath()).exists()) {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(sfx.getFilePath()));
                soundCache.put(sfx, sound);
                return sound;
            } else {
                Gdx.app.log("AudioManager", "SFX not found: " + sfx.getFilePath());
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to load sound " + sfx + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Resolves the music file path for a track by checking supported extensions (.ogg, .mp3, .wav),
     * configured naming aliases, and search directories. Returns null if no file is present.
     */
    public String resolveMusicFile(MusicTrack track) {
        if (track == null) return null;
        for (String dir : MUSIC_SEARCH_DIRS) {
            for (String alias : track.getAliases()) {
                for (String ext : SUPPORTED_MUSIC_EXTENSIONS) {
                    String path = dir + alias + ext;
                    if (Gdx.files != null && Gdx.files.internal(path).exists()) {
                        return path;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Plays the specified background music track with looping enabled and smooth cross-fade.
     */
    public void playMusic(MusicTrack track) {
        playMusic(track, true, false);
    }

    /**
     * Plays the specified background music track with optional looping and smooth cross-fade.
     */
    public void playMusic(MusicTrack track, boolean loop) {
        playMusic(track, loop, false);
    }

    /**
     * Plays the specified background music track with optional looping and immediate/cross-fade transition.
     * When immediate is true, any playing track is cleanly replaced without cross-fade delay.
     * If no audio file exists for the track, audio is cleanly stopped and remains silent.
     */
    public void playMusic(MusicTrack track, boolean loop, boolean immediate) {
        if (track == null) return;
        if (currentTrack == track && currentMusic != null && currentMusic.isPlaying()) {
            return; // Already playing this track
        }

        try {
            String resolvedPath = resolveMusicFile(track);
            if (resolvedPath == null) {
                // No audio file found for this track; stop current music and remain silent
                stopMusic();
                currentTrack = track;
                return;
            }

            if (immediate || currentMusic == null) {
                stopMusic();
                currentTrack = track;
                currentMusic = Gdx.audio.newMusic(Gdx.files.internal(resolvedPath));
                currentMusic.setLooping(loop);
                currentMusic.setVolume(musicVolume);
                currentMusic.play();
                isCrossFading = false;
            } else {
                if (nextMusic != null) {
                    try {
                        nextMusic.stop();
                        nextMusic.dispose();
                    } catch (Exception ignored) {}
                    nextMusic = null;
                }
                // Initiate smooth cross-fade
                nextTrack = track;
                nextLoop = loop;
                nextMusic = Gdx.audio.newMusic(Gdx.files.internal(resolvedPath));
                nextMusic.setLooping(loop);
                nextMusic.setVolume(0f);
                nextMusic.play();
                isCrossFading = true;
                fadeElapsed = 0f;
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error playing music " + track + ": " + e.getMessage());
        }
    }

    /**
     * Updates cross-fading and music volume interpolation per frame.
     */
    public void update(float delta) {
        if (!isCrossFading) return;

        fadeElapsed += delta;
        float progress = MathUtils.clamp(fadeElapsed / FADE_DURATION, 0f, 1f);

        if (currentMusic != null) {
            currentMusic.setVolume(MathUtils.lerp(musicVolume, 0f, progress));
        }
        if (nextMusic != null) {
            nextMusic.setVolume(MathUtils.lerp(0f, musicVolume, progress));
        }

        if (progress >= 1.0f) {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
            }
            currentMusic = nextMusic;
            currentTrack = nextTrack;
            if (currentMusic != null) {
                currentMusic.setVolume(musicVolume);
            }
            nextMusic = null;
            nextTrack = null;
            isCrossFading = false;
        }
    }

    /**
     * Stops the currently playing background music.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentTrack = null;
        }
        if (nextMusic != null) {
            nextMusic.stop();
            nextMusic.dispose();
            nextMusic = null;
            nextTrack = null;
        }
        isCrossFading = false;
    }

    /**
     * Plays a sound effect at default SFX volume.
     */
    public void playSound(SoundEffect sfx) {
        playSound(sfx, 1.0f, 0.0f);
    }

    /**
     * Plays a sound effect with a custom volume multiplier.
     */
    public void playSound(SoundEffect sfx, float volumeMultiplier) {
        playSound(sfx, volumeMultiplier, 0.0f);
    }

    /**
     * Plays a sound effect with volume multiplier and pitch variance.
     * Pitch variance creates organic acoustic variety (e.g. for footsteps or dice rolls).
     */
    public void playSound(SoundEffect sfx, float volumeMultiplier, float pitchVariance) {
        if (sfx == null || sfxVolume <= 0.001f) return;

        try {
            Sound sound = loadSound(sfx);
            if (sound != null) {
                float vol = MathUtils.clamp(sfxVolume * volumeMultiplier, 0f, 1f);
                float pitch = 1.0f;
                if (pitchVariance > 0f) {
                    pitch = MathUtils.random(1.0f - pitchVariance, 1.0f + pitchVariance);
                }
                sound.play(vol, pitch, 0.0f);
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error playing SFX " + sfx + ": " + e.getMessage());
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        if (currentMusic != null && !isCrossFading) {
            currentMusic.setVolume(this.musicVolume);
        }
        savePreferences();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = MathUtils.clamp(volume, 0f, 1f);
        savePreferences();
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    private void savePreferences() {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
            prefs.putFloat(PREF_MUSIC, musicVolume);
            prefs.putFloat(PREF_SFX, sfxVolume);
            prefs.flush();
        } catch (Exception e) {
            // Silently ignore if preferences are unavailable during headless testing
        }
    }

    public void dispose() {
        stopMusic();
        for (Sound sound : soundCache.values()) {
            try {
                sound.dispose();
            } catch (Exception ignored) {}
        }
        soundCache.clear();
        initialized = false;
    }
}
