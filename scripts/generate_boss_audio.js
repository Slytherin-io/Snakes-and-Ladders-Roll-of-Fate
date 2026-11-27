const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const SAMPLE_RATE = 44100;

function makeStereoWav(leftSamples, rightSamples, sampleRate = SAMPLE_RATE) {
    const numSamples = leftSamples.length;
    const buffer = Buffer.alloc(44 + numSamples * 4);
    buffer.write('RIFF', 0);
    buffer.writeUInt32LE(36 + numSamples * 4, 4);
    buffer.write('WAVE', 8);
    buffer.write('fmt ', 12);
    buffer.writeUInt32LE(16, 16);
    buffer.writeUInt16LE(1, 20); // PCM
    buffer.writeUInt16LE(2, 22); // Stereo
    buffer.writeUInt32LE(sampleRate, 24);
    buffer.writeUInt32LE(sampleRate * 4, 28);
    buffer.writeUInt16LE(4, 32);
    buffer.writeUInt16LE(16, 34);
    buffer.write('data', 36);
    buffer.writeUInt32LE(numSamples * 4, 40);

    let offset = 44;
    for (let i = 0; i < numSamples; i++) {
        const l = Math.tanh(leftSamples[i]);
        const r = Math.tanh(rightSamples[i]);
        buffer.writeInt16LE(Math.floor(l < 0 ? l * 32768 : l * 32767), offset);
        buffer.writeInt16LE(Math.floor(r < 0 ? r * 32768 : r * 32767), offset + 2);
        offset += 4;
    }
    return buffer;
}

function saveStereoAudio(name, leftSamples, rightSamples, isMusic = false) {
    const dir = isMusic ? 'assets/audio/music' : 'assets/audio/sfx';
    const wavPath = path.join(dir, `${name}.wav`);
    const oggPath = path.join(dir, `${name}.ogg`);

    fs.writeFileSync(wavPath, makeStereoWav(leftSamples, rightSamples));
    try {
        const q = isMusic ? 6 : 5;
        execSync(`ffmpeg -y -i "${wavPath}" -c:a libvorbis -q:a ${q} "${oggPath}"`, { stdio: 'ignore' });
        if (fs.existsSync(oggPath) && fs.statSync(oggPath).size > 100) {
            fs.unlinkSync(wavPath);
            console.log(`[OK] Generated ${oggPath} (${Math.round(fs.statSync(oggPath).size / 1024)} KB)`);
        }
    } catch (e) {
        console.warn(`[WARN] ffmpeg failed for ${name}, keeping WAV:`, e.message);
    }
}

const SEMITONES = {
    'D2': -31, 'F2': -28, 'G2': -26, 'A2': -24, 'Bb2': -23, 'C3': -21,
    'D3': -19, 'E3': -17, 'F3': -16, 'F#3': -15, 'G3': -14, 'A3': -12, 'Bb3': -11, 'C4': -9,
    'D4': -7, 'E4': -5, 'F4': -4, 'F#4': -3, 'G4': -2, 'A4': 0, 'Bb4': 1, 'C5': 3,
    'D5': 5, 'E5': 7, 'F5': 8, 'F#5': 9, 'G5': 10, 'A5': 12, 'Bb5': 13, 'C6': 15, 'D6': 17
};

function noteFreq(name) {
    return 440 * Math.pow(2, (SEMITONES[name] ?? 0) / 12);
}

function panMix(left, right, idx, sample, pan = 0) {
    const gL = Math.cos(((pan + 1) * Math.PI) / 4);
    const gR = Math.sin(((pan + 1) * Math.PI) / 4);
    left[idx] += sample * gL;
    right[idx] += sample * gR;
}

// ---------------------------------------------------------------------------
// 1. sfx_boss_appear.ogg (3.2s) - Winged descent, stone slam & celestial chime
// ---------------------------------------------------------------------------
function generateBossAppearSfx() {
    const dur = 3.2;
    const n = Math.floor(SAMPLE_RATE * dur);
    const left = new Float32Array(n);
    const right = new Float32Array(n);

    const chord = [noteFreq('D3'), noteFreq('A3'), noteFreq('D4'), noteFreq('A4')];
    let pink = 0;

    for (let i = 0; i < n; i++) {
        const t = i / SAMPLE_RATE;

        // Swell from 0.0 -> 1.25s then decay cleanly
        if (t < 2.2) {
            const env = t < 1.25
                ? Math.pow(t / 1.25, 1.5) * 0.42
                : Math.exp(-(t - 1.25) * 3.2) * 0.42;
            let brassL = 0, brassR = 0;
            for (let c = 0; c < chord.length; c++) {
                const f = chord[c];
                const vib = 1 + 0.003 * Math.sin(2 * Math.PI * 5 * t + c);
                brassL += Math.sin(2 * Math.PI * f * vib * t) + 0.35 * Math.sin(2 * Math.PI * f * 2 * t);
                brassR += Math.sin(2 * Math.PI * (f * 1.003) * vib * t) + 0.35 * Math.sin(2 * Math.PI * f * 2 * t);
            }
            left[i] += (brassL / chord.length) * env;
            right[i] += (brassR / chord.length) * env;
        }

        // Wing swoop noise (0.45s -> 1.35s)
        const white = Math.random() * 2 - 1;
        pink = 0.85 * pink + 0.15 * white;
        if (t >= 0.45 && t <= 1.4) {
            const wt = (t - 0.45) / 0.95;
            const wingEnv = Math.sin(wt * Math.PI) * (0.28 + 0.15 * Math.sin(2 * Math.PI * 3.5 * wt));
            panMix(left, right, i, pink * wingEnv, 0.25);
        }

        // Heavy pedestal stone slam at t = 1.25s
        if (t >= 1.25 && t < 2.6) {
            const st = t - 1.25;
            const pitchDrop = 28 + 56 * Math.exp(-st * 12);
            const subEnv = Math.exp(-st * 3.8) * 0.85;
            const rumbleEnv = Math.exp(-st * 9.5) * 0.45;
            const slam = Math.sin(2 * Math.PI * pitchDrop * st) * subEnv + pink * rumbleEnv;
            left[i] += slam;
            right[i] += slam;
        }

        // Radiant temple chime shimmer (1.25s -> 3.2s)
        if (t >= 1.25) {
            const ct = t - 1.25;
            const chimeEnv = Math.min(1, ct / 0.04) * Math.exp(-ct * 1.6) * 0.32;
            const f1 = noteFreq('D5');
            const f2 = noteFreq('A5');
            const f3 = noteFreq('E5');
            left[i] += (Math.sin(2 * Math.PI * f1 * ct) + 0.6 * Math.sin(2 * Math.PI * f2 * ct)) * chimeEnv;
            right[i] += (Math.sin(2 * Math.PI * (f1 * 1.002) * ct) + 0.6 * Math.sin(2 * Math.PI * f3 * ct)) * chimeEnv;
        }
    }
    saveStereoAudio('sfx_boss_appear', left, right, false);
}

// ---------------------------------------------------------------------------
// 2. sfx_boss_roar.ogg (1.35s) - Solar Lion Chimera roar + solar flare burst
// ---------------------------------------------------------------------------
function generateBossRoarSfx() {
    const dur = 1.35;
    const n = Math.floor(SAMPLE_RATE * dur);
    const left = new Float32Array(n);
    const right = new Float32Array(n);
    let noiseLP = 0;

    for (let i = 0; i < n; i++) {
        const t = i / SAMPLE_RATE;
        const env = Math.min(1, t / 0.08) * Math.exp(-t * 2.1);
        const f0 = 135 - 52 * Math.min(1, t / 0.9);
        const raw =
            0.55 * Math.sin(2 * Math.PI * f0 * t) +
            0.40 * Math.sin(2 * Math.PI * f0 * 2.02 * t + 0.4 * Math.sin(2 * Math.PI * 38 * t)) +
            0.25 * Math.sin(2 * Math.PI * f0 * 3.01 * t);

        const white = Math.random() * 2 - 1;
        noiseLP = 0.68 * noiseLP + 0.32 * white;
        const flare = noiseLP * Math.exp(-t * 3.8) * 0.42;

        left[i] += raw * env * 0.65 + flare * 0.9;
        right[i] += (raw * 0.98) * env * 0.65 + flare * 1.05;
    }
    saveStereoAudio('sfx_boss_roar', left, right, false);
}

// ---------------------------------------------------------------------------
// 3. sfx_dawn_rebirth.ogg (2.6s) - Golden sunrise arpeggio upon defeating Warden
// ---------------------------------------------------------------------------
function generateDawnRebirthSfx() {
    const dur = 2.6;
    const n = Math.floor(SAMPLE_RATE * dur);
    const left = new Float32Array(n);
    const right = new Float32Array(n);
    const notes = ['D4', 'F#4', 'A4', 'D5', 'E5', 'F#5', 'A5'];

    notes.forEach((note, idx) => {
        const start = idx * 0.14;
        const freq = noteFreq(note);
        const pan = -0.35 + (idx / (notes.length - 1)) * 0.7;
        const startIdx = Math.floor(start * SAMPLE_RATE);

        for (let i = startIdx; i < n; i++) {
            const dt = (i - startIdx) / SAMPLE_RATE;
            const decay = (idx === notes.length - 1) ? 1.35 : 2.1;
            const env = Math.min(1, dt / 0.015) * Math.exp(-dt * decay) * 0.34;
            const tone =
                Math.sin(2 * Math.PI * freq * dt) +
                0.4 * Math.sin(2 * Math.PI * freq * 2 * dt) +
                0.15 * Math.sin(2 * Math.PI * freq * 3 * dt);
            panMix(left, right, i, tone * env, pan);
        }
    });
    saveStereoAudio('sfx_dawn_rebirth', left, right, false);
}

// ---------------------------------------------------------------------------
// 4. music_boss.ogg (16 bars @ 150 BPM -> 25.6s loop) - Summit Trial Theme
// ---------------------------------------------------------------------------
function generateBossMusic() {
    const bpm = 150;
    const beatSec = 60 / bpm; // 0.4s
    const barSec = beatSec * 4; // 1.6s
    const bars = 16;
    const totalDur = bars * barSec; // 25.6s
    const n = Math.floor(SAMPLE_RATE * totalDur);
    const left = new Float32Array(n);
    const right = new Float32Array(n);

    // 16-bar tragic & heroic progression in D minor / A harmonic minor
    const prog = [
        ['D3', 'F3', 'A3'], ['D3', 'F3', 'A3'], ['Bb2', 'D3', 'F3'], ['C3', 'E3', 'G3'],
        ['D3', 'F3', 'A3'], ['Bb2', 'D3', 'F3'], ['G2', 'Bb2', 'D3'], ['A2', 'C#3' in SEMITONES ? 'C3' : 'E3', 'E3'],
        ['D3', 'F3', 'A3'], ['F3', 'A3', 'C4'], ['G3', 'Bb3', 'D4'], ['A3', 'E4', 'A4'],
        ['Bb2', 'D3', 'F3'], ['G2', 'Bb2', 'D3'], ['A2', 'E3', 'A3'], ['A2', 'E3', 'A3']
    ];
    prog[7] = ['A2', 'E3', 'A3'];

    const bassRoots = ['D2', 'D2', 'Bb2', 'C3', 'D2', 'Bb2', 'G2', 'A2', 'D2', 'F2', 'G2', 'A2', 'Bb2', 'G2', 'A2', 'A2'];

    // Driving timpani + low string ostinato per 8th note
    for (let bar = 0; bar < bars; bar++) {
        const root = noteFreq(bassRoots[bar]);
        const chord = prog[bar];

        // Sustained somber pad strings
        const bStart = Math.floor(bar * barSec * SAMPLE_RATE);
        const bEnd = Math.min(n, Math.floor((bar + 1) * barSec * SAMPLE_RATE));
        for (let i = bStart; i < bEnd; i++) {
            const lt = (i - bStart) / SAMPLE_RATE;
            const env = Math.min(1, lt / 0.12) * Math.min(1, (barSec - lt) / 0.12) * 0.16;
            let pL = 0, pR = 0;
            for (const cName of chord) {
                const f = noteFreq(cName);
                pL += Math.sin(2 * Math.PI * f * lt) + 0.25 * Math.sin(2 * Math.PI * f * 2 * lt);
                pR += Math.sin(2 * Math.PI * (f * 1.003) * lt) + 0.25 * Math.sin(2 * Math.PI * f * 2 * lt);
            }
            left[i] += (pL / 3) * env;
            right[i] += (pR / 3) * env;
        }

        // Eighth-note urgent bass pulse + timpani hits on beats 1 and 3
        for (let eighth = 0; eighth < 8; eighth++) {
            const tHit = bar * barSec + eighth * (beatSec / 2);
            const hStart = Math.floor(tHit * SAMPLE_RATE);
            const hEnd = Math.min(n, hStart + Math.floor(0.28 * SAMPLE_RATE));
            const accent = (eighth % 4 === 0) ? 1.0 : ((eighth % 2 === 0) ? 0.72 : 0.48);

            for (let i = hStart; i < hEnd; i++) {
                const dt = (i - hStart) / SAMPLE_RATE;
                const env = Math.exp(-dt * 14) * 0.24 * accent;
                const s = Math.sin(2 * Math.PI * root * dt) + 0.45 * Math.sin(2 * Math.PI * root * 2 * dt);
                panMix(left, right, i, s * env, (eighth % 2 === 0) ? -0.15 : 0.15);
            }

            if (eighth === 0 || eighth === 4 || (bar % 4 === 3 && eighth === 6)) {
                const tEnd = Math.min(n, hStart + Math.floor(0.32 * SAMPLE_RATE));
                for (let i = hStart; i < tEnd; i++) {
                    const dt = (i - hStart) / SAMPLE_RATE;
                    const df = 42 + 48 * Math.exp(-dt * 24);
                    const drum = Math.sin(2 * Math.PI * df * dt) * Math.exp(-dt * 8.5) * 0.36;
                    left[i] += drum;
                    right[i] += drum;
                }
            }
        }
    }

    // Tragic + Heroic Lead Brass/Pipe Line
    const lead = [
        [0, 0, 1.5, 'A4'], [0, 1.5, 0.5, 'Bb4'], [0, 2, 1, 'C5'], [0, 3, 1, 'D5'],
        [1, 0, 2, 'A4'], [1, 2, 1.5, 'G4'], [1, 3.5, 0.5, 'F4'],
        [2, 0, 2, 'F4'], [2, 2, 2, 'G4'],
        [3, 0, 3.5, 'E4'],
        [4, 0, 1.5, 'A4'], [4, 1.5, 0.5, 'D5'], [4, 2, 1.5, 'F5'], [4, 3.5, 0.5, 'E5'],
        [5, 0, 2, 'D5'], [5, 2, 2, 'C5'],
        [6, 0, 2, 'Bb4'], [6, 2, 1, 'A4'], [6, 3, 1, 'G4'],
        [7, 0, 3.5, 'A4'],
        [8, 0, 1.5, 'D5'], [8, 1.5, 0.5, 'E5'], [8, 2, 2, 'F5'],
        [9, 0, 2, 'A5'], [9, 2, 2, 'G5'],
        [10, 0, 1.5, 'F5'], [10, 1.5, 0.5, 'E5'], [10, 2, 2, 'D5'],
        [11, 0, 3.5, 'E5'],
        [12, 0, 1.5, 'F5'], [12, 1.5, 0.5, 'G5'], [12, 2, 2, 'A5'],
        [13, 0, 2, 'G5'], [13, 2, 2, 'F5'],
        [14, 0, 2, 'E5'], [14, 2, 2, 'D5'],
        [15, 0, 3.5, 'E5']
    ];

    for (const [bar, beat, durBeats, note] of lead) {
        const startT = bar * barSec + beat * beatSec;
        const durSec = durBeats * beatSec;
        const freq = noteFreq(note);
        const i0 = Math.floor(startT * SAMPLE_RATE);
        const i1 = Math.min(n, i0 + Math.floor((durSec + 0.15) * SAMPLE_RATE));
        for (let i = i0; i < i1; i++) {
            const dt = (i - i0) / SAMPLE_RATE;
            const env = Math.min(1, dt / 0.04) * Math.min(1, Math.max(0, (durSec + 0.15 - dt) / 0.18)) * 0.25;
            const vib = 1 + 0.004 * Math.sin(2 * Math.PI * 5.2 * dt);
            const tone =
                Math.sin(2 * Math.PI * freq * vib * dt) +
                0.32 * Math.sin(2 * Math.PI * freq * 2 * dt) +
                0.12 * Math.sin(2 * Math.PI * freq * 3 * dt);
            panMix(left, right, i, tone * env, 0.05);
        }
    }

    saveStereoAudio('music_boss', left, right, true);
}

console.log('=== Generating Dawn Warden Boss Audio Suite ===');
generateBossAppearSfx();
generateBossRoarSfx();
generateDawnRebirthSfx();
generateBossMusic();
console.log('=== Boss Audio Generation Complete ===');
