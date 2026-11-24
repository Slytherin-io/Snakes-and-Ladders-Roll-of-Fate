const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const SAMPLE_RATE = 44100;

// Ensure output directories exist
fs.mkdirSync('assets/audio/sfx', { recursive: true });
fs.mkdirSync('assets/audio/music', { recursive: true });

function makeWav(samples, sampleRate = SAMPLE_RATE) {
    const numSamples = samples.length;
    const buffer = Buffer.alloc(44 + numSamples * 2);
    buffer.write('RIFF', 0);
    buffer.writeUInt32LE(36 + numSamples * 2, 4);
    buffer.write('WAVE', 8);
    buffer.write('fmt ', 12);
    buffer.writeUInt32LE(16, 16);
    buffer.writeUInt16LE(1, 20); // PCM
    buffer.writeUInt16LE(1, 22); // Mono
    buffer.writeUInt32LE(sampleRate, 24);
    buffer.writeUInt32LE(sampleRate * 2, 28);
    buffer.writeUInt16LE(2, 32);
    buffer.writeUInt16LE(16, 34);
    buffer.write('data', 36);
    buffer.writeUInt32LE(numSamples * 2, 40);

    for (let i = 0; i < numSamples; i++) {
        const s = Math.max(-1, Math.min(1, samples[i]));
        buffer.writeInt16LE(Math.floor(s < 0 ? s * 32768 : s * 32767), 44 + i * 2);
    }
    return buffer;
}

function saveAudio(name, floatSamples, isMusic = false) {
    const dir = isMusic ? 'assets/audio/music' : 'assets/audio/sfx';
    const wavPath = path.join(dir, `${name}.wav`);
    const oggPath = path.join(dir, `${name}.ogg`);

    const wavBuf = makeWav(floatSamples);
    fs.writeFileSync(wavPath, wavBuf);

    try {
        execSync(`ffmpeg -y -i "${wavPath}" -c:a libvorbis -q:a 5 "${oggPath}"`, { stdio: 'ignore' });
        if (fs.existsSync(oggPath) && fs.statSync(oggPath).size > 100) {
            fs.unlinkSync(wavPath);
            console.log(`[OK] Generated ${oggPath} (${Math.round(fs.statSync(oggPath).size / 1024)} KB)`);
        } else {
            console.log(`[OK] Kept ${wavPath}`);
        }
    } catch (e) {
        console.warn(`[WARN] ffmpeg failed for ${name}, keeping WAV:`, e.message);
    }
}

// ==========================================
// SYNTHESIS TOOLKIT
// ==========================================

function noteFreq(semitonesFromA4) {
    return 440 * Math.pow(2, semitonesFromA4 / 12);
}

// Standard semitone offsets from A4 (A4 = 0)
const NOTES = {
    'C3': -21, 'D3': -19, 'E3': -17, 'F3': -16, 'G3': -14, 'A3': -12, 'B3': -10,
    'C4': -9,  'D4': -7,  'E4': -5,  'F4': -4,  'G4': -2,  'A4': 0,   'B4': 2,
    'C5': 3,   'D5': 5,   'E5': 7,   'F5': 8,   'G5': 10,  'A5': 12,  'B5': 14,
    'C6': 15,  'D6': 17,  'E6': 19,  'F6': 20,  'G6': 22,  'A6': 24,  'B6': 26,
    'C7': 27,
    'F#3': -15, 'G#3': -13, 'A#3': -11,
    'C#4': -8,  'D#4': -6,  'F#4': -3,  'G#4': -1,  'A#4': 1,
    'C#5': 4,   'D#5': 6,   'F#5': 9,   'G#5': 11,  'A#5': 13,
    'REST': null
};

function getFreq(name) {
    if (!name || name === 'REST') return null;
    return noteFreq(NOTES[name]);
}

// Add note to track buffer with envelope and instrument synthesis
function addNote(buffer, startSec, durationSec, freq, instrument = 'flute', volume = 0.5) {
    if (!freq) return;
    const startIdx = Math.floor(startSec * SAMPLE_RATE);
    const length = Math.floor(durationSec * SAMPLE_RATE);

    for (let i = 0; i < length; i++) {
        const idx = startIdx + i;
        if (idx >= buffer.length) break;
        const t = i / SAMPLE_RATE;
        const progress = i / length;

        let sample = 0;

        if (instrument === 'flute') {
            // Warm wooden pipe with gentle vibrato and soft overtones
            const vibrato = Math.sin(2 * Math.PI * 5.2 * t) * (progress > 0.2 ? 0.015 : 0.0);
            const f = freq * (1 + vibrato);
            const env = Math.sin(Math.min(Math.PI, progress * Math.PI)); // smooth bell envelope
            sample = (Math.sin(2 * Math.PI * f * t) * 0.75 +
                      Math.sin(4 * Math.PI * f * t) * 0.20 +
                      Math.sin(6 * Math.PI * f * t) * 0.05) * env;
        } else if (instrument === 'lute' || instrument === 'harp') {
            // Plucked string with fast attack and exponential harmonic decay
            const env = Math.exp(-progress * 4.5);
            sample = (Math.sin(2 * Math.PI * freq * t) * 0.6 +
                      Math.sin(4 * Math.PI * freq * t) * 0.25 * Math.exp(-progress * 7) +
                      Math.sin(6 * Math.PI * freq * t) * 0.15 * Math.exp(-progress * 10)) * env;
        } else if (instrument === 'bell') {
            // Glockenspiel / crystal chime with inharmonic overtones
            const env = Math.exp(-progress * 3.5);
            sample = (Math.sin(2 * Math.PI * freq * t) * 0.5 +
                      Math.sin(2 * Math.PI * freq * 2.76 * t) * 0.3 * Math.exp(-progress * 5) +
                      Math.sin(2 * Math.PI * freq * 5.40 * t) * 0.2 * Math.exp(-progress * 8)) * env;
        } else if (instrument === 'bass') {
            // Warm resonant acoustic / sub bass
            const env = Math.exp(-progress * 3.0);
            sample = (Math.sin(2 * Math.PI * freq * t) * 0.7 +
                      Math.sin(4 * Math.PI * freq * t) * 0.2 +
                      Math.sin(6 * Math.PI * freq * t) * 0.1) * env;
        } else if (instrument === 'brass') {
            // Dramatic saw/pulse brass for battles and Act 3
            const env = Math.min(1, progress * 15) * Math.exp(-progress * 1.8);
            let s = 0;
            for (let h = 1; h <= 6; h++) {
                s += (Math.sin(2 * Math.PI * freq * h * t) / h);
            }
            sample = s * 0.4 * env;
        } else if (instrument === 'pad') {
            // Ethereal lush ambient chords
            const env = Math.sin(Math.min(Math.PI, progress * Math.PI));
            sample = (Math.sin(2 * Math.PI * freq * t) +
                      Math.sin(2 * Math.PI * (freq * 1.004) * t) +
                      Math.sin(2 * Math.PI * (freq * 0.996) * t)) * 0.3 * env;
        }

        buffer[idx] += sample * volume;
    }
}

// Add drum hits to buffer
function addDrum(buffer, startSec, type = 'kick', volume = 0.5) {
    const startIdx = Math.floor(startSec * SAMPLE_RATE);
    let length = Math.floor(0.25 * SAMPLE_RATE);

    if (type === 'kick') {
        length = Math.floor(0.18 * SAMPLE_RATE);
        for (let i = 0; i < length; i++) {
            const idx = startIdx + i;
            if (idx >= buffer.length) break;
            const t = i / SAMPLE_RATE;
            const f = 110 * Math.exp(-t * 28);
            const env = Math.exp(-t * 18);
            buffer[idx] += Math.sin(2 * Math.PI * f * t) * env * volume;
        }
    } else if (type === 'snare') {
        length = Math.floor(0.15 * SAMPLE_RATE);
        for (let i = 0; i < length; i++) {
            const idx = startIdx + i;
            if (idx >= buffer.length) break;
            const t = i / SAMPLE_RATE;
            const noise = (Math.random() * 2 - 1) * Math.exp(-t * 22);
            const tone = Math.sin(2 * Math.PI * 180 * t) * Math.exp(-t * 30);
            buffer[idx] += (noise * 0.7 + tone * 0.3) * volume;
        }
    } else if (type === 'shaker' || type === 'hihat') {
        length = Math.floor(0.06 * SAMPLE_RATE);
        for (let i = 0; i < length; i++) {
            const idx = startIdx + i;
            if (idx >= buffer.length) break;
            const t = i / SAMPLE_RATE;
            const noise = (Math.random() * 2 - 1) * Math.exp(-t * 60);
            buffer[idx] += noise * volume;
        }
    } else if (type === 'wardrum') {
        length = Math.floor(0.4 * SAMPLE_RATE);
        for (let i = 0; i < length; i++) {
            const idx = startIdx + i;
            if (idx >= buffer.length) break;
            const t = i / SAMPLE_RATE;
            const f = 85 * Math.exp(-t * 12);
            const env = Math.exp(-t * 9);
            const grit = (Math.random() * 2 - 1) * Math.exp(-t * 35) * 0.2;
            buffer[idx] += (Math.sin(2 * Math.PI * f * t) + grit) * env * volume;
        }
    }
}

// ==========================================
// 1. GENERATE ENGAGING SOUND EFFECTS (SFX)
// ==========================================
console.log('--- Generating Sound Effects ---');

// 1.1 UI CLICK (Crisp wooden button press)
{
    const len = Math.floor(0.05 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const click = (Math.random() * 2 - 1) * Math.exp(-t * 300) * 0.3;
        const wood = Math.sin(2 * Math.PI * 1150 * t) * Math.exp(-t * 120) * 0.5 +
                     Math.sin(2 * Math.PI * 340 * t) * Math.exp(-t * 70) * 0.3;
        buf[i] = (click + wood);
    }
    saveAudio('sfx_click', buf);
}

// 1.2 DICE ROLL (Tumbling bone/wood dice rattling on table)
{
    const len = Math.floor(0.75 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    const impacts = [
        { t: 0.00, f: 520, amp: 0.8 },
        { t: 0.08, f: 440, amp: 0.7 },
        { t: 0.17, f: 580, amp: 0.65 },
        { t: 0.28, f: 410, amp: 0.55 },
        { t: 0.41, f: 490, amp: 0.45 },
        { t: 0.56, f: 380, amp: 0.40 },
        { t: 0.63, f: 360, amp: 0.30 }
    ];
    for (const imp of impacts) {
        const start = Math.floor(imp.t * SAMPLE_RATE);
        const impLen = Math.floor(0.06 * SAMPLE_RATE);
        for (let i = 0; i < impLen; i++) {
            const idx = start + i;
            if (idx >= len) break;
            const t = i / SAMPLE_RATE;
            const noise = (Math.random() * 2 - 1) * Math.exp(-t * 180) * 0.25;
            const tone = Math.sin(2 * Math.PI * imp.f * t) * Math.exp(-t * 90) * 0.75;
            buf[idx] += (noise + tone) * imp.amp;
        }
    }
    saveAudio('sfx_dice_roll', buf);
}

// 1.3 STEP / WALK (Crisp stone/moss hop)
{
    const len = Math.floor(0.09 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const thud = Math.sin(2 * Math.PI * 160 * t) * Math.exp(-t * 60) * 0.6;
        const grit = (Math.random() * 2 - 1) * Math.exp(-t * 110) * 0.35;
        buf[i] = thud + grit;
    }
    saveAudio('sfx_step', buf);
}

// 1.4 FOG REVEAL (Sparkling discovery flare)
{
    const len = Math.floor(1.1 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    const chimes = [
        { t: 0.00, f: 659.25 }, // E5
        { t: 0.07, f: 987.77 }, // B5
        { t: 0.14, f: 1318.5 }, // E6
        { t: 0.21, f: 1661.2 }, // G#6
        { t: 0.28, f: 1975.5 }  // B6
    ];
    for (const c of chimes) {
        const start = Math.floor(c.t * SAMPLE_RATE);
        for (let i = start; i < len; i++) {
            const t = (i - start) / SAMPLE_RATE;
            const bell = Math.sin(2 * Math.PI * c.f * t) * Math.exp(-t * 5.0) * 0.35 +
                         Math.sin(2 * Math.PI * c.f * 2.76 * t) * Math.exp(-t * 8.0) * 0.15;
            buf[i] += bell;
        }
    }
    // High shimmer sparkle
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const shimmer = Math.sin(2 * Math.PI * 3200 * t) * Math.sin(2 * Math.PI * 18 * t) * Math.exp(-t * 3.5) * 0.12;
        buf[i] += shimmer;
    }
    saveAudio('sfx_fog_reveal', buf);
}

// 1.5 LADDER WINDUP (0.85s golden ascension crescendo)
{
    const len = Math.floor(0.85 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const progress = i / len;
        const f = 220 + progress * 140; // Frequency rises 220Hz -> 360Hz
        const swell = Math.pow(progress, 1.4); // Crescendo
        const tone = (Math.sin(2 * Math.PI * f * t) * 0.5 +
                      Math.sin(2 * Math.PI * (f * 1.5) * t) * 0.3 +
                      Math.sin(2 * Math.PI * (f * 2.0) * t) * 0.2) * swell;
        // Rhythmic golden sparkle pulses
        const pulse = Math.sin(2 * Math.PI * 14 * progress) > 0.5 ? 0.2 * progress : 0.0;
        const spark = Math.sin(2 * Math.PI * (1200 + progress * 800) * t) * pulse;
        buf[i] = (tone + spark) * 0.8;
    }
    saveAudio('sfx_ladder_windup', buf);
}

// 1.6 LADDER SOAR (Airy soaring wind whoosh + magical glissando)
{
    const len = Math.floor(1.5 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const progress = i / len;
        // Parabolic flight curve matches arc
        const arc = Math.sin(progress * Math.PI);
        // Whoosh (filtered noise)
        const whoosh = (Math.random() * 2 - 1) * arc * (0.2 + 0.25 * Math.sin(progress * 12));
        // Soaring celestial glissando (rising and gently resolving)
        const glissFreq = 440 + arc * 600;
        const celestial = Math.sin(2 * Math.PI * glissFreq * t) * arc * 0.4 +
                          Math.sin(2 * Math.PI * glissFreq * 2 * t) * arc * 0.2;
        buf[i] = whoosh + celestial;
    }
    saveAudio('sfx_ladder_soar', buf);
}

// 1.7 LADDER LAND (Golden temple chime + solid touchdown impact)
{
    const len = Math.floor(0.85 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        // Solid platform thud
        const thud = Math.sin(2 * Math.PI * 90 * t) * Math.exp(-t * 22) * 0.7;
        // Resonant golden bell (F5 = 698Hz)
        const bell = (Math.sin(2 * Math.PI * 698.46 * t) * 0.5 +
                      Math.sin(2 * Math.PI * 1396.9 * t) * 0.25 +
                      Math.sin(2 * Math.PI * 1927.7 * t) * 0.15) * Math.exp(-t * 4.2);
        buf[i] = thud + bell;
    }
    saveAudio('sfx_ladder_land', buf);
}

// 1.8 SNAKE HISS (Menacing serpent hiss and rattle)
{
    const len = Math.floor(0.75 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const env = Math.sin(Math.min(Math.PI, (i / len) * Math.PI));
        // 22Hz rattle modulation on noise
        const rattle = 0.6 + 0.4 * Math.sin(2 * Math.PI * 26 * t);
        const hiss = (Math.random() * 2 - 1) * rattle * env;
        // Low ominous undertone
        const growl = Math.sin(2 * Math.PI * (110 - t * 40) * t) * env * 0.25;
        buf[i] = hiss * 0.6 + growl;
    }
    saveAudio('sfx_snake_hiss', buf);
}

// 1.9 SNAKE SLIDE (Descending tumble and coil rustle)
{
    const len = Math.floor(1.1 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const progress = i / len;
        // Descending whistle 750Hz -> 180Hz
        const f = 750 * Math.exp(-progress * 1.5);
        const whistle = Math.sin(2 * Math.PI * f * t) * (1 - progress * 0.5) * 0.35;
        // Rustle
        const rustle = (Math.random() * 2 - 1) * Math.sin(progress * 16) * 0.25 * (1 - progress);
        buf[i] = whistle + rustle;
    }
    saveAudio('sfx_snake_slide', buf);
}

// 1.10 POTION (Uncork pop + liquid glug)
{
    const len = Math.floor(0.75 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    // Cork pop at t=0
    const popLen = Math.floor(0.04 * SAMPLE_RATE);
    for (let i = 0; i < popLen; i++) {
        const t = i / SAMPLE_RATE;
        const f = 600 + (t / 0.04) * 900;
        buf[i] += Math.sin(2 * Math.PI * f * t) * Math.exp(-t * 70) * 0.7;
    }
    // Glug 1 at t=0.12, Glug 2 at t=0.32
    const glugs = [0.12, 0.32];
    for (const gt of glugs) {
        const start = Math.floor(gt * SAMPLE_RATE);
        const gLen = Math.floor(0.12 * SAMPLE_RATE);
        for (let i = 0; i < gLen; i++) {
            const idx = start + i;
            if (idx >= len) break;
            const t = i / SAMPLE_RATE;
            const f = 380 - (t / 0.12) * 80;
            buf[idx] += Math.sin(2 * Math.PI * f * t) * Math.exp(-t * 28) * 0.6;
        }
    }
    saveAudio('sfx_potion', buf);
}

// 1.11 EQUIP (Steel blade unsheathe & buckle snap)
{
    const len = Math.floor(0.55 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const progress = i / len;
        // Steel scrape rising
        const f = 1600 + progress * 2400;
        const steel = (Math.sin(2 * Math.PI * f * t) + (Math.random() * 2 - 1) * 0.3) *
                      Math.exp(-progress * 5) * 0.45;
        // Buckle snap at t=0.15
        let snap = 0;
        if (t > 0.12 && t < 0.25) {
            const st = t - 0.12;
            snap = Math.sin(2 * Math.PI * 620 * st) * Math.exp(-st * 40) * 0.6;
        }
        buf[i] = steel + snap;
    }
    saveAudio('sfx_equip', buf);
}

// 1.12 SCOUT FLARE (Torch ignition and expanding reveal pulse)
{
    const len = Math.floor(0.95 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        // Flame swoosh
        const flame = (Math.random() * 2 - 1) * Math.sin(t * 8) * Math.exp(-t * 3.0) * 0.35;
        // Radiant high chime harmonic
        const chime = (Math.sin(2 * Math.PI * 1250 * t) * 0.4 +
                       Math.sin(2 * Math.PI * 2500 * t) * 0.2) * Math.exp(-t * 3.5);
        buf[i] = flame + chime;
    }
    saveAudio('sfx_scout', buf);
}

// 1.13 ATTACK SLASH (Blade whoosh impact)
{
    const len = Math.floor(0.32 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const progress = i / len;
        const f = 3200 * Math.exp(-progress * 4.0);
        const whoosh = (Math.random() * 2 - 1) * Math.sin(progress * Math.PI) * 0.5;
        const blade = Math.sin(2 * Math.PI * f * t) * (1 - progress) * 0.5;
        buf[i] = whoosh + blade;
    }
    saveAudio('sfx_attack', buf);
}

// 1.14 DEFEND GUARD (Heavy iron shield clank)
{
    const len = Math.floor(0.65 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const impact = (Math.random() * 2 - 1) * Math.exp(-t * 80) * 0.4;
        const ring = (Math.sin(2 * Math.PI * 940 * t) * 0.5 +
                      Math.sin(2 * Math.PI * 1620 * t) * 0.35 +
                      Math.sin(2 * Math.PI * 280 * t) * 0.4) * Math.exp(-t * 7.0);
        buf[i] = impact + ring;
    }
    saveAudio('sfx_defend', buf);
}

// 1.15 HIT MONSTER (Reptilian pain crunch)
{
    const len = Math.floor(0.38 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const crunch = (Math.random() * 2 - 1) * Math.exp(-t * 35) * 0.55;
        const groan = Math.sin(2 * Math.PI * (140 - t * 60) * t) * Math.exp(-t * 18) * 0.5;
        buf[i] = crunch + groan;
    }
    saveAudio('sfx_hit_monster', buf);
}

// 1.16 HIT PLAYER (Heavy blunt body thud)
{
    const len = Math.floor(0.35 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    for (let i = 0; i < len; i++) {
        const t = i / SAMPLE_RATE;
        const punch = Math.sin(2 * Math.PI * 85 * t) * Math.exp(-t * 25) * 0.8;
        const grit = (Math.random() * 2 - 1) * Math.exp(-t * 50) * 0.3;
        buf[i] = punch + grit;
    }
    saveAudio('sfx_hit_player', buf);
}

// 1.17 SANCTUARY HEAL (Cathedral choir & holy bell chime)
{
    const len = Math.floor(1.8 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    const chord = [293.66, 369.99, 440.0, 587.33]; // D, F#, A, D5
    for (const f of chord) {
        for (let i = 0; i < len; i++) {
            const t = i / SAMPLE_RATE;
            const env = Math.sin(Math.min(Math.PI, (i / len) * Math.PI * 1.5)) * Math.exp(-t * 1.2);
            buf[i] += Math.sin(2 * Math.PI * f * t) * env * 0.2;
        }
    }
    saveAudio('sfx_sanctuary', buf);
}

// 1.18 LEVEL UP (Radiant RPG arpeggio)
{
    const len = Math.floor(1.4 * SAMPLE_RATE);
    const buf = new Float32Array(len);
    const notes = [
        { t: 0.00, f: 523.25 }, // C5
        { t: 0.10, f: 659.25 }, // E5
        { t: 0.20, f: 783.99 }, // G5
        { t: 0.30, f: 1046.5 }, // C6
        { t: 0.45, f: 1318.5 }  // E6
    ];
    for (const n of notes) {
        const start = Math.floor(n.t * SAMPLE_RATE);
        for (let i = start; i < len; i++) {
            const t = (i - start) / SAMPLE_RATE;
            const bell = (Math.sin(2 * Math.PI * n.f * t) * 0.6 +
                          Math.sin(2 * Math.PI * n.f * 2 * t) * 0.3) * Math.exp(-t * 3.5);
            buf[i] += bell * 0.35;
        }
    }
    saveAudio('sfx_level_up', buf);
}

// ==========================================
// 2. GENERATE ON-THEME MUSIC LOOPS
// ==========================================
console.log('\n--- Generating On-Theme Music Loops ---');

// Helper to normalize and master track audio
function masterTrack(buf, peakTarget = 0.85) {
    let max = 0;
    for (let i = 0; i < buf.length; i++) {
        const abs = Math.abs(buf[i]);
        if (abs > max) max = abs;
    }
    if (max > 0.001) {
        const factor = peakTarget / max;
        for (let i = 0; i < buf.length; i++) {
            buf[i] *= factor;
        }
    }
}

// 2.1 MAIN MENU THEME: "Fireside Tavern & Whispering Dice"
// Cozy, whimsical medieval tavern/adventure exploration melody (lute, flute, percussion)
// 104 BPM, Key: A minor / C major, Duration: 24 bars (~27.69s loop)
{
    const bpm = 104;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 12; // 12 bars seamless loop
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Rhythm Drums & Tambourine
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        if (b % 4 === 0 || b % 4 === 2) {
            addDrum(buf, t, 'kick', 0.45);
        } else {
            addDrum(buf, t, 'snare', 0.35);
        }
        addDrum(buf, t + beatSec * 0.5, 'shaker', 0.18);
    }

    // Acoustic Bassline (A - F - C - G)
    const bassSeq = [
        'A3', 'A3', 'F3', 'F3', 'C4', 'C4', 'G3', 'G3',
        'A3', 'A3', 'F3', 'F3', 'C4', 'C4', 'E3', 'E3'
    ];
    for (let i = 0; i < totalBars; i++) {
        const root = bassSeq[i % bassSeq.length];
        const t = i * barSec;
        addNote(buf, t, barSec * 0.45, getFreq(root), 'bass', 0.5);
        addNote(buf, t + beatSec * 2, barSec * 0.45, getFreq(root), 'bass', 0.45);
    }

    // Lute Plucked Arpeggios (Gentle medieval lute backing)
    const lutePatterns = [
        ['A4', 'C5', 'E5', 'C5'], // Am
        ['F4', 'A4', 'C5', 'A4'], // F
        ['C4', 'E4', 'G4', 'E4'], // C
        ['G4', 'B4', 'D5', 'B4']  // G
    ];
    for (let bar = 0; bar < totalBars; bar++) {
        const pat = lutePatterns[bar % lutePatterns.length];
        for (let beat = 0; beat < 4; beat++) {
            const note = pat[beat % pat.length];
            addNote(buf, bar * barSec + beat * beatSec, beatSec * 0.85, getFreq(note), 'lute', 0.28);
        }
    }

    // Lead Wooden Flute Melody
    const fluteMelody = [
        // Bar 1-4
        { bar: 0, beat: 0, dur: 1.5, n: 'E5' },
        { bar: 0, beat: 1.5, dur: 1.0, n: 'A5' },
        { bar: 0, beat: 2.5, dur: 1.5, n: 'B5' },
        { bar: 1, beat: 0, dur: 2.0, n: 'C6' },
        { bar: 1, beat: 2, dur: 2.0, n: 'B5' },
        { bar: 2, beat: 0, dur: 1.5, n: 'G5' },
        { bar: 2, beat: 1.5, dur: 1.0, n: 'E5' },
        { bar: 2, beat: 2.5, dur: 1.5, n: 'D5' },
        { bar: 3, beat: 0, dur: 3.5, n: 'E5' },
        // Bar 5-8
        { bar: 4, beat: 0, dur: 1.5, n: 'A5' },
        { bar: 4, beat: 1.5, dur: 1.0, n: 'B5' },
        { bar: 4, beat: 2.5, dur: 1.5, n: 'C6' },
        { bar: 5, beat: 0, dur: 2.0, n: 'D6' },
        { bar: 5, beat: 2, dur: 2.0, n: 'E6' },
        { bar: 6, beat: 0, dur: 1.5, n: 'C6' },
        { bar: 6, beat: 1.5, dur: 1.0, n: 'B5' },
        { bar: 6, beat: 2.5, dur: 1.5, n: 'A5' },
        { bar: 7, beat: 0, dur: 3.5, n: 'A5' },
        // Bar 9-12 (playful cadence back to start)
        { bar: 8, beat: 0, dur: 1.5, n: 'C6' },
        { bar: 8, beat: 1.5, dur: 1.5, n: 'B5' },
        { bar: 8, beat: 3.0, dur: 1.0, n: 'A5' },
        { bar: 9, beat: 0, dur: 2.0, n: 'F5' },
        { bar: 9, beat: 2, dur: 2.0, n: 'G5' },
        { bar: 10, beat: 0, dur: 1.5, n: 'E5' },
        { bar: 10, beat: 1.5, dur: 1.0, n: 'D5' },
        { bar: 10, beat: 2.5, dur: 1.5, n: 'C5' },
        { bar: 11, beat: 0, dur: 2.0, n: 'B4' },
        { bar: 11, beat: 2, dur: 2.0, n: 'A4' }
    ];
    for (const m of fluteMelody) {
        if (m.bar < totalBars) {
            const t = m.bar * barSec + m.beat * beatSec;
            addNote(buf, t, m.dur * beatSec, getFreq(m.n), 'flute', 0.45);
        }
    }

    masterTrack(buf);
    saveAudio('music_menu', buf, true);
}

// 2.2 ACT I THEME: "Emerald Canopy" (Verdant Woods)
// Celtic fantasy journey, airy wooden flutes, lively strummed harp, optimistic pace
// 116 BPM, Key: G major / D major, 12 bars (~24.82s loop)
{
    const bpm = 116;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 12;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Celtic Bodhrán & Shaker
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        if (b % 4 === 0 || b % 4 === 2) addDrum(buf, t, 'kick', 0.42);
        if (b % 4 === 1 || b % 4 === 3) addDrum(buf, t, 'shaker', 0.25);
        addDrum(buf, t + beatSec * 0.5, 'shaker', 0.15);
    }

    // Walking Bass
    const bass = ['G3', 'G3', 'C4', 'C4', 'D4', 'D4', 'G3', 'G3', 'E3', 'E3', 'C4', 'D4'];
    for (let i = 0; i < totalBars; i++) {
        const t = i * barSec;
        addNote(buf, t, barSec * 0.4, getFreq(bass[i]), 'bass', 0.5);
        addNote(buf, t + beatSec * 2, barSec * 0.4, getFreq(bass[i]), 'bass', 0.4);
    }

    // Celtic Harp Arpeggios
    const harp = [
        ['G4', 'B4', 'D5', 'G5'],
        ['C4', 'E4', 'G4', 'C5'],
        ['D4', 'F#4', 'A4', 'D5'],
        ['G4', 'B4', 'D5', 'B4']
    ];
    for (let bar = 0; bar < totalBars; bar++) {
        const chord = harp[bar % harp.length];
        for (let b = 0; b < 4; b++) {
            addNote(buf, bar * barSec + b * beatSec, beatSec * 0.8, getFreq(chord[b]), 'harp', 0.28);
        }
    }

    // Lively Forest Pipe Melody
    const melody = [
        { bar: 0, beat: 0, dur: 1.0, n: 'D5' },
        { bar: 0, beat: 1, dur: 1.0, n: 'G5' },
        { bar: 0, beat: 2, dur: 1.0, n: 'A5' },
        { bar: 0, beat: 3, dur: 1.0, n: 'B5' },
        { bar: 1, beat: 0, dur: 2.0, n: 'C6' },
        { bar: 1, beat: 2, dur: 1.5, n: 'B5' },
        { bar: 1, beat: 3.5, dur: 0.5, n: 'A5' },
        { bar: 2, beat: 0, dur: 2.0, n: 'B5' },
        { bar: 2, beat: 2, dur: 1.0, n: 'G5' },
        { bar: 2, beat: 3, dur: 1.0, n: 'E5' },
        { bar: 3, beat: 0, dur: 3.5, n: 'D5' },

        { bar: 4, beat: 0, dur: 1.0, n: 'E5' },
        { bar: 4, beat: 1, dur: 1.0, n: 'G5' },
        { bar: 4, beat: 2, dur: 1.0, n: 'B5' },
        { bar: 4, beat: 3, dur: 1.0, n: 'D6' },
        { bar: 5, beat: 0, dur: 2.0, n: 'C6' },
        { bar: 5, beat: 2, dur: 2.0, n: 'B5' },
        { bar: 6, beat: 0, dur: 1.5, n: 'A5' },
        { bar: 6, beat: 1.5, dur: 1.0, n: 'B5' },
        { bar: 6, beat: 2.5, dur: 1.5, n: 'A5' },
        { bar: 7, beat: 0, dur: 3.5, n: 'G5' },

        { bar: 8, beat: 0, dur: 2.0, n: 'B5' },
        { bar: 8, beat: 2, dur: 2.0, n: 'D6' },
        { bar: 9, beat: 0, dur: 2.0, n: 'E6' },
        { bar: 9, beat: 2, dur: 2.0, n: 'D6' },
        { bar: 10, beat: 0, dur: 1.5, n: 'B5' },
        { bar: 10, beat: 1.5, dur: 1.5, n: 'A5' },
        { bar: 10, beat: 3.0, dur: 1.0, n: 'G5' },
        { bar: 11, beat: 0, dur: 2.0, n: 'A5' },
        { bar: 11, beat: 2, dur: 2.0, n: 'G5' }
    ];
    for (const m of melody) {
        if (m.bar < totalBars) {
            addNote(buf, m.bar * barSec + m.beat * beatSec, m.dur * beatSec, getFreq(m.n), 'flute', 0.42);
        }
    }

    masterTrack(buf);
    saveAudio('music_act1', buf, true);
}

// 2.3 ACT II THEME: "Azure Caverns" (Subterranean Depths)
// Mysterious, ambient, crystal glockenspiel drops, ethereal pads, echoing harp
// 90 BPM, Key: E minor / B minor, 12 bars (~32.0s loop)
{
    const bpm = 90;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 12;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Deep Subterranean Resonance
    for (let b = 0; b < totalBars; b++) {
        const t = b * barSec;
        addDrum(buf, t, 'wardrum', 0.4);
    }

    // Ethereal Pad Chords (Em - C - G - Bm)
    const pads = [
        ['E4', 'G4', 'B4'],
        ['C4', 'E4', 'G4'],
        ['G3', 'B3', 'D4'],
        ['B3', 'D#4', 'F#4']
    ];
    for (let bar = 0; bar < totalBars; bar++) {
        const chord = pads[bar % pads.length];
        for (const n of chord) {
            addNote(buf, bar * barSec, barSec * 0.95, getFreq(n), 'pad', 0.22);
        }
    }

    // Crystal Glockenspiel Drops & Melodic Echoes
    const bells = [
        { bar: 0, beat: 1, n: 'B5' },
        { bar: 0, beat: 2.5, n: 'E6' },
        { bar: 1, beat: 0, n: 'G6' },
        { bar: 1, beat: 2, n: 'F#6' },
        { bar: 2, beat: 1, n: 'D6' },
        { bar: 2, beat: 3, n: 'B5' },
        { bar: 3, beat: 0, n: 'E6' },

        { bar: 4, beat: 0.5, n: 'G5' },
        { bar: 4, beat: 2, n: 'B5' },
        { bar: 5, beat: 0, n: 'D6' },
        { bar: 5, beat: 2.5, n: 'C6' },
        { bar: 6, beat: 1, n: 'B5' },
        { bar: 6, beat: 3, n: 'A5' },
        { bar: 7, beat: 0, n: 'E5' },

        { bar: 8, beat: 1, n: 'E6' },
        { bar: 8, beat: 2.5, n: 'F#6' },
        { bar: 9, beat: 0, n: 'G6' },
        { bar: 9, beat: 2, n: 'A6' },
        { bar: 10, beat: 0.5, n: 'B6' },
        { bar: 10, beat: 2, n: 'F#6' },
        { bar: 11, beat: 0, n: 'E6' }
    ];
    for (const b of bells) {
        if (b.bar < totalBars) {
            addNote(buf, b.bar * barSec + b.beat * beatSec, 2.5, getFreq(b.n), 'bell', 0.35);
        }
    }

    masterTrack(buf);
    saveAudio('music_act2', buf, true);
}

// 2.4 ACT III THEME: "Crimson Caldera" (Volcanic Summit)
// Intense, martial war drums, brooding brass ostinato, boss rush tension
// 132 BPM, Key: D minor, 12 bars (~21.82s loop)
{
    const bpm = 132;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 12;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Heavy War Drums & Marching Beat
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        if (b % 4 === 0 || b % 4 === 2) addDrum(buf, t, 'wardrum', 0.55);
        if (b % 4 === 1 || b % 4 === 3) addDrum(buf, t, 'snare', 0.45);
        addDrum(buf, t + beatSec * 0.5, 'hihat', 0.2);
    }

    // Menacing Bass Ostinato (D - D - D - F - G - D)
    for (let bar = 0; bar < totalBars; bar++) {
        const t = bar * barSec;
        addNote(buf, t, beatSec * 0.4, getFreq('D3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 0.5, beatSec * 0.4, getFreq('D3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 1.5, beatSec * 0.4, getFreq('D3'), 'bass', 0.5);
        addNote(buf, t + beatSec * 2.0, beatSec * 0.4, getFreq('F3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 3.0, beatSec * 0.4, getFreq('G3'), 'bass', 0.6);
    }

    // Dramatic Staccato Brass Chords & Leads
    const brassRiffs = [
        { bar: 0, beat: 0, dur: 0.8, n: 'D4' },
        { bar: 0, beat: 1, dur: 0.8, n: 'F4' },
        { bar: 0, beat: 2, dur: 1.5, n: 'A4' },
        { bar: 1, beat: 0, dur: 1.5, n: 'G4' },
        { bar: 1, beat: 2, dur: 1.5, n: 'F4' },
        { bar: 2, beat: 0, dur: 3.0, n: 'D4' },

        { bar: 4, beat: 0, dur: 0.8, n: 'D4' },
        { bar: 4, beat: 1, dur: 0.8, n: 'F4' },
        { bar: 4, beat: 2, dur: 1.5, n: 'A4' },
        { bar: 5, beat: 0, dur: 1.5, n: 'C5' },
        { bar: 5, beat: 2, dur: 1.5, n: 'A4' },
        { bar: 6, beat: 0, dur: 3.0, n: 'D5' },

        { bar: 8, beat: 0, dur: 1.0, n: 'F5' },
        { bar: 8, beat: 1.5, dur: 1.0, n: 'E5' },
        { bar: 8, beat: 3.0, dur: 1.0, n: 'D5' },
        { bar: 9, beat: 0, dur: 2.0, n: 'C#5' },
        { bar: 9, beat: 2, dur: 2.0, n: 'D5' },
        { bar: 10, beat: 0, dur: 1.5, n: 'E5' },
        { bar: 10, beat: 2, dur: 1.5, n: 'F5' },
        { bar: 11, beat: 0, dur: 3.0, n: 'D5' }
    ];
    for (const b of brassRiffs) {
        if (b.bar < totalBars) {
            addNote(buf, b.bar * barSec + b.beat * beatSec, b.dur * beatSec, getFreq(b.n), 'brass', 0.45);
        }
    }

    masterTrack(buf);
    saveAudio('music_act3', buf, true);
}

// 2.5 BATTLE THEME: "Serpent's Gambit"
// Driving turn-based RPG combat battle theme, fast rhythm, tense heroic drive
// 140 BPM, Key: G minor, 12 bars (~20.57s loop)
{
    const bpm = 140;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 12;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Punchy Combat Drums
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        if (b % 4 === 0 || b % 4 === 2) addDrum(buf, t, 'kick', 0.55);
        if (b % 4 === 1 || b % 4 === 3) addDrum(buf, t, 'snare', 0.5);
        addDrum(buf, t + beatSec * 0.5, 'hihat', 0.22);
    }

    // Driving 16th-note style Bass (G - G - Bb - C)
    for (let bar = 0; bar < totalBars; bar++) {
        const t = bar * barSec;
        addNote(buf, t, beatSec * 0.4, getFreq('G3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 0.5, beatSec * 0.4, getFreq('G3'), 'bass', 0.5);
        addNote(buf, t + beatSec * 1.0, beatSec * 0.4, getFreq('G3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 2.0, beatSec * 0.4, getFreq('A#3'), 'bass', 0.6);
        addNote(buf, t + beatSec * 3.0, beatSec * 0.4, getFreq('C4'), 'bass', 0.6);
    }

    // Heroic Battle Brass Lead
    const battleRiffs = [
        { bar: 0, beat: 0, dur: 1.0, n: 'G4' },
        { bar: 0, beat: 1, dur: 0.5, n: 'A#4' },
        { bar: 0, beat: 1.5, dur: 1.0, n: 'C5' },
        { bar: 0, beat: 2.5, dur: 1.5, n: 'D5' },
        { bar: 1, beat: 0, dur: 2.0, n: 'F5' },
        { bar: 1, beat: 2, dur: 2.0, n: 'D5' },
        { bar: 2, beat: 0, dur: 1.0, n: 'C5' },
        { bar: 2, beat: 1, dur: 1.0, n: 'A#4' },
        { bar: 2, beat: 2, dur: 2.0, n: 'G4' },

        { bar: 4, beat: 0, dur: 1.0, n: 'G4' },
        { bar: 4, beat: 1, dur: 0.5, n: 'A#4' },
        { bar: 4, beat: 1.5, dur: 1.0, n: 'C5' },
        { bar: 4, beat: 2.5, dur: 1.5, n: 'D5' },
        { bar: 5, beat: 0, dur: 2.0, n: 'F5' },
        { bar: 5, beat: 2, dur: 2.0, n: 'G5' },
        { bar: 6, beat: 0, dur: 1.5, n: 'D5' },
        { bar: 6, beat: 1.5, dur: 1.5, n: 'C5' },
        { bar: 6, beat: 3.0, dur: 1.0, n: 'A#4' },
        { bar: 7, beat: 0, dur: 3.5, n: 'G4' },

        { bar: 8, beat: 0, dur: 1.5, n: 'D5' },
        { bar: 8, beat: 1.5, dur: 1.5, n: 'F5' },
        { bar: 8, beat: 3.0, dur: 1.0, n: 'G5' },
        { bar: 9, beat: 0, dur: 2.0, n: 'F5' },
        { bar: 9, beat: 2, dur: 2.0, n: 'D5' },
        { bar: 10, beat: 0, dur: 1.5, n: 'C5' },
        { bar: 10, beat: 1.5, dur: 1.0, n: 'A#4' },
        { bar: 10, beat: 2.5, dur: 1.5, n: 'C5' },
        { bar: 11, beat: 0, dur: 3.5, n: 'G4' }
    ];
    for (const b of battleRiffs) {
        if (b.bar < totalBars) {
            addNote(buf, b.bar * barSec + b.beat * beatSec, b.dur * beatSec, getFreq(b.n), 'brass', 0.42);
        }
    }

    masterTrack(buf);
    saveAudio('music_battle', buf, true);
}

// 2.6 VICTORY FANFARE: "Triumph of Fate"
// Glorious celebratory brass march & joyful bell chimes
// 120 BPM, Duration: 6 bars (~12.0s)
{
    const bpm = 120;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 6;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Marching Drums
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        if (b % 4 === 0 || b % 4 === 2) addDrum(buf, t, 'kick', 0.5);
        if (b % 4 === 1 || b % 4 === 3) addDrum(buf, t, 'snare', 0.45);
    }

    // Triumphant Fanfare Melody (C - G - C - E - G - C6)
    const fanfare = [
        { t: 0.0, dur: 0.4, n: 'C4' },
        { t: 0.4, dur: 0.4, n: 'E4' },
        { t: 0.8, dur: 0.4, n: 'G4' },
        { t: 1.2, dur: 1.5, n: 'C5' },

        { t: 2.8, dur: 0.4, n: 'G4' },
        { t: 3.2, dur: 0.4, n: 'A4' },
        { t: 3.6, dur: 0.4, n: 'B4' },
        { t: 4.0, dur: 2.0, n: 'C5' },

        { t: 6.0, dur: 0.4, n: 'C5' },
        { t: 6.4, dur: 0.4, n: 'D5' },
        { t: 6.8, dur: 0.4, n: 'E5' },
        { t: 7.2, dur: 1.5, n: 'F5' },
        { t: 8.7, dur: 1.0, n: 'E5' },
        { t: 9.7, dur: 1.0, n: 'D5' },
        { t: 10.7, dur: 3.5, n: 'C5' }
    ];
    for (const f of fanfare) {
        addNote(buf, f.t, f.dur, getFreq(f.n), 'brass', 0.5);
        addNote(buf, f.t, f.dur, getFreq(f.n), 'bell', 0.3);
    }

    masterTrack(buf);
    saveAudio('music_victory', buf, true);
}

// 2.7 DEFEAT THEME: "Fallen in the Mist"
// Somber, reflective minor piano/cello chords fading into the mist
// 75 BPM, Duration: 4 bars (~12.8s)
{
    const bpm = 75;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 4;
    const totalSec = totalBars * barSec;
    const buf = new Float32Array(Math.floor(totalSec * SAMPLE_RATE));

    // Somber Minor Pad Chords (Dm - Am - Bb - Dm)
    const somberChords = [
        ['D3', 'F3', 'A3', 'D4'],
        ['A2', 'C3', 'E3', 'A3'],
        ['A#2', 'D3', 'F3', 'A#3'],
        ['D2', 'A2', 'D3', 'F3']
    ];
    for (let bar = 0; bar < totalBars; bar++) {
        const chord = somberChords[bar];
        for (const n of chord) {
            addNote(buf, bar * barSec, barSec * 0.95, getFreq(n), 'pad', 0.35);
        }
    }

    // Melancholy Bell Drops
    const bellNotes = [
        { t: 0.5, n: 'A4' },
        { t: 2.0, n: 'F4' },
        { t: 3.8, n: 'E4' },
        { t: 5.5, n: 'D4' },
        { t: 7.2, n: 'C4' },
        { t: 9.0, n: 'D4' }
    ];
    for (const b of bellNotes) {
        addNote(buf, b.t, 2.5, getFreq(b.n), 'bell', 0.3);
    }

    masterTrack(buf);
    saveAudio('music_defeat', buf, true);
}

console.log('=== All Audio Assets Successfully Generated! ===');
