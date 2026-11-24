const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const SAMPLE_RATE = 44100;

function makeStereoWav(leftSamples, rightSamples, sampleRate = SAMPLE_RATE) {
    const numSamples = leftSamples.length;
    const numChannels = 2;
    const bytesPerSample = 2;
    const blockAlign = numChannels * bytesPerSample;
    const byteRate = sampleRate * blockAlign;
    const dataSize = numSamples * blockAlign;

    const buffer = Buffer.alloc(44 + dataSize);
    buffer.write('RIFF', 0);
    buffer.writeUInt32LE(36 + dataSize, 4);
    buffer.write('WAVE', 8);
    buffer.write('fmt ', 12);
    buffer.writeUInt32LE(16, 16);
    buffer.writeUInt16LE(1, 20); // PCM
    buffer.writeUInt16LE(numChannels, 22); // Stereo
    buffer.writeUInt32LE(sampleRate, 24);
    buffer.writeUInt32LE(byteRate, 28);
    buffer.writeUInt16LE(blockAlign, 32);
    buffer.writeUInt16LE(16, 34); // 16-bit
    buffer.write('data', 36);
    buffer.writeUInt32LE(dataSize, 40);

    let offset = 44;
    for (let i = 0; i < numSamples; i++) {
        // Soft saturation (tanh warmth) to prevent harsh digital clipping
        const l = Math.tanh(leftSamples[i]);
        const r = Math.tanh(rightSamples[i]);

        buffer.writeInt16LE(Math.floor(l < 0 ? l * 32768 : l * 32767), offset);
        buffer.writeInt16LE(Math.floor(r < 0 ? r * 32768 : r * 32767), offset + 2);
        offset += 4;
    }
    return buffer;
}

function saveStereoAudio(name, leftSamples, rightSamples, isMusic = true) {
    const dir = isMusic ? 'assets/audio/music' : 'assets/audio/sfx';
    const wavPath = path.join(dir, `${name}.wav`);
    const oggPath = path.join(dir, `${name}.ogg`);

    const wavBuf = makeStereoWav(leftSamples, rightSamples);
    fs.writeFileSync(wavPath, wavBuf);

    try {
        execSync(`ffmpeg -y -i "${wavPath}" -c:a libvorbis -q:a 6 "${oggPath}"`, { stdio: 'ignore' });
        if (fs.existsSync(oggPath) && fs.statSync(oggPath).size > 100) {
            fs.unlinkSync(wavPath);
            console.log(`[OK] Generated stereo ${oggPath} (${Math.round(fs.statSync(oggPath).size / 1024)} KB)`);
        } else {
            console.log(`[OK] Kept ${wavPath}`);
        }
    } catch (e) {
        console.warn(`[WARN] ffmpeg failed for ${name}, keeping WAV:`, e.message);
    }
}

// Semitone frequency table (A4 = 440 Hz)
const NOTE_SEMITONES = {
    'C2': -33, 'D2': -31, 'E2': -29, 'F2': -28, 'G2': -26, 'A2': -24, 'B2': -22,
    'C3': -21, 'D3': -19, 'E3': -17, 'F3': -16, 'G3': -14, 'A3': -12, 'B3': -10,
    'C4': -9,  'D4': -7,  'E4': -5,  'F4': -4,  'G4': -2,  'A4': 0,   'B4': 2,
    'C5': 3,   'D5': 5,   'E5': 7,   'F5': 8,   'G5': 10,  'A5': 12,  'B5': 14,
    'C6': 15,  'D6': 17,  'E6': 19,  'F6': 20,  'G6': 22,  'A6': 24,  'B6': 26,
    'C#3': -20, 'D#3': -18, 'F#3': -15, 'G#3': -13, 'A#3': -11,
    'C#4': -8,  'D#4': -6,  'F#4': -3,  'G#4': -1,  'A#4': 1,
    'C#5': 4,   'D#5': 6,   'F#5': 9,   'G#5': 11,  'A#5': 13,
    'F#6': 21
};

function noteFreq(name) {
    if (!name || !NOTE_SEMITONES[name]) return 440;
    return 440 * Math.pow(2, NOTE_SEMITONES[name] / 12);
}

// Karplus-Strong plucked acoustic string generator
function renderKarplusString(freq, durationSec, damping = 0.992) {
    const N = Math.max(2, Math.round(SAMPLE_RATE / freq));
    const delayLine = new Float32Array(N);
    // Initial pluck noise burst with lowpass smoothing for warmer wooden tone
    let lastNoise = 0;
    for (let i = 0; i < N; i++) {
        const white = Math.random() * 2 - 1;
        lastNoise = 0.4 * lastNoise + 0.6 * white;
        delayLine[i] = lastNoise;
    }

    const totalSamples = Math.round(durationSec * SAMPLE_RATE);
    const out = new Float32Array(totalSamples);
    let readIdx = 0;

    for (let i = 0; i < totalSamples; i++) {
        const nextIdx = (readIdx + 1) % N;
        // Two-point averaging filter with decay damping
        delayLine[readIdx] = damping * 0.5 * (delayLine[readIdx] + delayLine[nextIdx]);
        out[i] = delayLine[readIdx];
        readIdx = nextIdx;
    }
    return out;
}

// Strum a full chord across multiple strings with realistic finger-sweep delay
function addStrummedChord(leftBuf, rightBuf, startSec, durationSec, noteNames, volume = 0.35, pan = -0.2) {
    const strumDelay = 0.012; // 12ms between string plucks
    noteNames.forEach((n, idx) => {
        const freq = noteFreq(n);
        const stringStart = startSec + idx * strumDelay;
        const stringSamples = renderKarplusString(freq, durationSec, 0.993);
        const startIdx = Math.round(stringStart * SAMPLE_RATE);

        // Pan calculation
        const stringPan = pan + (idx / noteNames.length) * 0.2 - 0.1;
        const lGain = Math.cos((stringPan + 1) * Math.PI / 4) * volume;
        const rGain = Math.sin((stringPan + 1) * Math.PI / 4) * volume;

        for (let i = 0; i < stringSamples.length; i++) {
            const pos = startIdx + i;
            if (pos >= leftBuf.length) break;
            leftBuf[pos] += stringSamples[i] * lGain;
            rightBuf[pos] += stringSamples[i] * rGain;
        }
    });
}

// Warm Accordion / Concertina Reed Pad with dual-reed tremolo
function addAccordionChord(leftBuf, rightBuf, startSec, durationSec, noteNames, volume = 0.25, pan = 0.25) {
    const startIdx = Math.round(startSec * SAMPLE_RATE);
    const totalSamples = Math.round(durationSec * SAMPLE_RATE);

    const lGain = Math.cos((pan + 1) * Math.PI / 4) * volume;
    const rGain = Math.sin((pan + 1) * Math.PI / 4) * volume;

    for (let i = 0; i < totalSamples; i++) {
        const pos = startIdx + i;
        if (pos >= leftBuf.length) break;
        const t = i / SAMPLE_RATE;
        const progress = i / totalSamples;

        // Smooth swell & gentle release envelope
        const env = Math.sin(Math.min(Math.PI, progress * Math.PI * 1.05));
        const tremolo = 0.85 + 0.15 * Math.sin(2 * Math.PI * 4.2 * t);

        let chordSample = 0;
        for (const n of noteNames) {
            const f = noteFreq(n);
            // Rich reed harmonics: fundamental + 2nd + 3rd + detuned chorus reed (+3 cents)
            const fDetuned = f * 1.0025;
            const reed1 = Math.sin(2 * Math.PI * f * t) * 0.45 +
                          Math.sin(4 * Math.PI * f * t) * 0.25 +
                          Math.sin(6 * Math.PI * f * t) * 0.15;
            const reed2 = Math.sin(2 * Math.PI * fDetuned * t) * 0.45 +
                          Math.sin(4 * Math.PI * fDetuned * t) * 0.25;
            chordSample += (reed1 + reed2) * 0.5;
        }
        chordSample = (chordSample / noteNames.length) * env * tremolo;

        leftBuf[pos] += chordSample * lGain;
        rightBuf[pos] += chordSample * rGain;
    }
}

// Energetic Irish Wooden Flute / Whistle Lead
function addWhistleNote(leftBuf, rightBuf, startSec, durationSec, noteName, volume = 0.4, pan = 0.0) {
    if (!noteName || noteName === 'REST') return;
    const freq = noteFreq(noteName);
    const startIdx = Math.round(startSec * SAMPLE_RATE);
    const totalSamples = Math.round(durationSec * SAMPLE_RATE);

    const lGain = Math.cos((pan + 1) * Math.PI / 4) * volume;
    const rGain = Math.sin((pan + 1) * Math.PI / 4) * volume;

    for (let i = 0; i < totalSamples; i++) {
        const pos = startIdx + i;
        if (pos >= leftBuf.length) break;
        const t = i / SAMPLE_RATE;
        const progress = i / totalSamples;

        // Attack & vibrato envelope (vibrato intensifies as note sustains)
        const attack = Math.min(1.0, i / (0.025 * SAMPLE_RATE));
        const decay = Math.min(1.0, (totalSamples - i) / (0.03 * SAMPLE_RATE));
        const env = attack * decay;

        const vibRate = 5.4; // 5.4 Hz vibrato
        const vibDepth = progress > 0.3 ? 0.018 * (progress - 0.3) / 0.7 : 0.003;
        const f = freq * (1 + Math.sin(2 * Math.PI * vibRate * t) * vibDepth);

        // Wooden whistle timbre: strong fundamental + warm 2nd harmonic + subtle breath grit
        const tone = Math.sin(2 * Math.PI * f * t) * 0.75 +
                     Math.sin(4 * Math.PI * f * t) * 0.22 +
                     Math.sin(6 * Math.PI * f * t) * 0.06;
        const breath = (Math.random() * 2 - 1) * 0.04;
        const sample = (tone + breath) * env;

        leftBuf[pos] += sample * lGain;
        rightBuf[pos] += sample * rGain;

        // Subtle warm stereo delay/reverb (80ms left, 120ms right)
        const dL = pos + Math.round(0.08 * SAMPLE_RATE);
        const dR = pos + Math.round(0.12 * SAMPLE_RATE);
        if (dL < leftBuf.length) leftBuf[dL] += sample * lGain * 0.22;
        if (dR < rightBuf.length) rightBuf[dR] += sample * rGain * 0.22;
    }
}

// Bouncy Plucked Upright Bass
function addUprightBass(leftBuf, rightBuf, startSec, durationSec, noteName, volume = 0.5) {
    if (!noteName || noteName === 'REST') return;
    const freq = noteFreq(noteName);
    const startIdx = Math.round(startSec * SAMPLE_RATE);
    const totalSamples = Math.round(durationSec * SAMPLE_RATE);

    for (let i = 0; i < totalSamples; i++) {
        const pos = startIdx + i;
        if (pos >= leftBuf.length) break;
        const t = i / SAMPLE_RATE;
        const progress = i / totalSamples;

        // Bass string pluck envelope with deep sustain
        const env = Math.exp(-progress * 4.2);
        const pitchBend = 1.0 + 0.08 * Math.exp(-t * 30); // Punchy initial attack bend
        const f = freq * pitchBend;

        const body = Math.sin(2 * Math.PI * f * t) * 0.70 +
                     Math.sin(4 * Math.PI * f * t) * 0.25 * Math.exp(-progress * 6) +
                     Math.sin(6 * Math.PI * f * t) * 0.10 * Math.exp(-progress * 9);
        const sample = body * env * volume;

        // Bass centered
        leftBuf[pos] += sample * 0.7;
        rightBuf[pos] += sample * 0.7;
    }
}

// Energetic Tavern Percussion
function addTavernPercussion(leftBuf, rightBuf, startSec, type = 'kick', volume = 0.45) {
    const startIdx = Math.round(startSec * SAMPLE_RATE);

    if (type === 'kick') {
        // Deep wooden stomp
        const len = Math.round(0.18 * SAMPLE_RATE);
        for (let i = 0; i < len; i++) {
            const pos = startIdx + i;
            if (pos >= leftBuf.length) break;
            const t = i / SAMPLE_RATE;
            const f = 120 * Math.exp(-t * 24);
            const env = Math.exp(-t * 16);
            const s = Math.sin(2 * Math.PI * f * t) * env * volume;
            leftBuf[pos] += s * 0.75;
            rightBuf[pos] += s * 0.75;
        }
    } else if (type === 'clap' || type === 'snare') {
        // Crisp acoustic hand clap / tavern snare
        const len = Math.round(0.14 * SAMPLE_RATE);
        for (let i = 0; i < len; i++) {
            const pos = startIdx + i;
            if (pos >= leftBuf.length) break;
            const t = i / SAMPLE_RATE;
            const noise = (Math.random() * 2 - 1) * Math.exp(-t * 26);
            const snap = Math.sin(2 * Math.PI * 340 * t) * Math.exp(-t * 38) * 0.4;
            const s = (noise * 0.75 + snap) * volume;
            leftBuf[pos] += s * 0.7;
            rightBuf[pos] += s * 0.7;
        }
    } else if (type === 'tambourine') {
        // High sparkling tambourine jingle
        const len = Math.round(0.08 * SAMPLE_RATE);
        for (let i = 0; i < len; i++) {
            const pos = startIdx + i;
            if (pos >= leftBuf.length) break;
            const t = i / SAMPLE_RATE;
            const jingle = (Math.random() * 2 - 1) * Math.sin(2 * Math.PI * 4200 * t) * Math.exp(-t * 45);
            const s = jingle * volume;
            leftBuf[pos] += s * 0.8;
            rightBuf[pos] += s * 0.5;
        }
    } else if (type === 'bodhran') {
        // Rolling Celtic Bodhrán
        const len = Math.round(0.25 * SAMPLE_RATE);
        for (let i = 0; i < len; i++) {
            const pos = startIdx + i;
            if (pos >= leftBuf.length) break;
            const t = i / SAMPLE_RATE;
            const f = 95 * Math.exp(-t * 14);
            const env = Math.exp(-t * 11);
            const thud = Math.sin(2 * Math.PI * f * t) * env * volume;
            leftBuf[pos] += thud * 0.7;
            rightBuf[pos] += thud * 0.7;
        }
    }
}

// =========================================================================
// 1. MAIN MENU MUSIC: "The Dancing Hearth" (Energetic & Warm Tavern Anthem)
// 128 BPM, Key: C Major / A Minor, 16 bars (~30.0s seamless loop)
// =========================================================================
console.log('Generating Track 1: Energetic & Warm Main Menu Theme (music_menu)...');
{
    const bpm = 128;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 16;
    const totalSec = totalBars * barSec;
    const totalSamples = Math.round(totalSec * SAMPLE_RATE);

    const left = new Float32Array(totalSamples);
    const right = new Float32Array(totalSamples);

    // Chord Progression:
    // Bars 0-3:   C  - G  - Am - F
    // Bars 4-7:   C  - G  - F  - C
    // Bars 8-11:  Am - Em - F  - C
    // Bars 12-15: Dm - G  - F  - G
    const chords = [
        // 0-3
        { bar: 0, notes: ['C3', 'G3', 'C4', 'E4', 'G4', 'C5'], bass: 'C3' },
        { bar: 1, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 2, notes: ['A2', 'E3', 'A3', 'C4', 'E4', 'A4'], bass: 'A2' },
        { bar: 3, notes: ['F2', 'C3', 'F3', 'A3', 'C4', 'F4'], bass: 'F2' },
        // 4-7
        { bar: 4, notes: ['C3', 'G3', 'C4', 'E4', 'G4', 'C5'], bass: 'C3' },
        { bar: 5, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 6, notes: ['F2', 'C3', 'F3', 'A3', 'C4', 'F4'], bass: 'F2' },
        { bar: 7, notes: ['C3', 'G3', 'C4', 'E4', 'G4', 'C5'], bass: 'C3' },
        // 8-11
        { bar: 8, notes: ['A2', 'E3', 'A3', 'C4', 'E4', 'A4'], bass: 'A2' },
        { bar: 9, notes: ['E2', 'B2', 'E3', 'G3', 'B3', 'E4'], bass: 'E2' },
        { bar: 10, notes: ['F2', 'C3', 'F3', 'A3', 'C4', 'F4'], bass: 'F2' },
        { bar: 11, notes: ['C3', 'G3', 'C4', 'E4', 'G4', 'C5'], bass: 'C3' },
        // 12-15 (Triumphant turnaround back to C)
        { bar: 12, notes: ['D3', 'A3', 'D4', 'F4', 'A4', 'D5'], bass: 'D3' },
        { bar: 13, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 14, notes: ['F2', 'C3', 'F3', 'A3', 'C4', 'F4'], bass: 'F2' },
        { bar: 15, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' }
    ];

    // 1.1 Rhythm Guitar / Lute Strume (driving 4-strum rhythm per bar: 1, 2, 2.5, 3, 4)
    for (const c of chords) {
        const barStart = c.bar * barSec;
        // Warm accordion pad sustains throughout the bar
        addAccordionChord(left, right, barStart, barSec * 0.98, c.notes.slice(2, 5), 0.22, 0.35);

        // Strum pattern: down (beat 0), down-up (beat 1, 1.5), down (beat 2), down-up (beat 3, 3.5)
        addStrummedChord(left, right, barStart + 0 * beatSec, beatSec * 0.9, c.notes, 0.38, -0.3);
        addStrummedChord(left, right, barStart + 1 * beatSec, beatSec * 0.45, c.notes, 0.32, -0.28);
        addStrummedChord(left, right, barStart + 1.5 * beatSec, beatSec * 0.45, c.notes, 0.28, -0.25);
        addStrummedChord(left, right, barStart + 2 * beatSec, beatSec * 0.9, c.notes, 0.38, -0.3);
        addStrummedChord(left, right, barStart + 3 * beatSec, beatSec * 0.45, c.notes, 0.32, -0.28);
        addStrummedChord(left, right, barStart + 3.5 * beatSec, beatSec * 0.45, c.notes, 0.30, -0.25);

        // Walking Bassline
        addUprightBass(left, right, barStart + 0 * beatSec, beatSec * 0.95, c.bass, 0.55);
        addUprightBass(left, right, barStart + 2 * beatSec, beatSec * 0.95, c.bass, 0.48);
        // Step to next root on beat 3.5
        addUprightBass(left, right, barStart + 3.5 * beatSec, beatSec * 0.45, c.notes[1], 0.42);
    }

    // 1.2 Upbeat Tavern Drums & Hand Claps
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        // Kick on 0 and 2
        if (b % 4 === 0 || b % 4 === 2) {
            addTavernPercussion(left, right, t, 'kick', 0.55);
        }
        // Hand claps & snare on 1 and 3 (the offbeats that create that energetic tavern bounce!)
        if (b % 4 === 1 || b % 4 === 3) {
            addTavernPercussion(left, right, t, 'clap', 0.48);
            addTavernPercussion(left, right, t, 'tambourine', 0.35);
        }
        // Shaker 16th-note pulse
        addTavernPercussion(left, right, t + beatSec * 0.5, 'tambourine', 0.18);
    }

    // 1.3 Catchy & Joyful Wooden Flute Lead Melody
    const menuLead = [
        // Bar 0 (C)
        { bar: 0, beat: 0, dur: 1.0, n: 'G4' },
        { bar: 0, beat: 1, dur: 0.5, n: 'C5' },
        { bar: 0, beat: 1.5, dur: 0.5, n: 'D5' },
        { bar: 0, beat: 2, dur: 1.0, n: 'E5' },
        { bar: 0, beat: 3, dur: 1.0, n: 'G5' },

        // Bar 1 (G)
        { bar: 1, beat: 0, dur: 1.5, n: 'E5' },
        { bar: 1, beat: 1.5, dur: 0.5, n: 'D5' },
        { bar: 1, beat: 2, dur: 2.0, n: 'D5' },

        // Bar 2 (Am)
        { bar: 2, beat: 0, dur: 1.0, n: 'E5' },
        { bar: 2, beat: 1, dur: 0.5, n: 'A5' },
        { bar: 2, beat: 1.5, dur: 0.5, n: 'B5' },
        { bar: 2, beat: 2, dur: 1.0, n: 'C6' },
        { bar: 2, beat: 3, dur: 1.0, n: 'B5' },

        // Bar 3 (F)
        { bar: 3, beat: 0, dur: 1.5, n: 'A5' },
        { bar: 3, beat: 1.5, dur: 0.5, n: 'G5' },
        { bar: 3, beat: 2, dur: 2.0, n: 'A5' },

        // Bar 4 (C)
        { bar: 4, beat: 0, dur: 1.0, n: 'C6' },
        { bar: 4, beat: 1, dur: 1.0, n: 'G5' },
        { bar: 4, beat: 2, dur: 1.0, n: 'E5' },
        { bar: 4, beat: 3, dur: 1.0, n: 'G5' },

        // Bar 5 (G)
        { bar: 5, beat: 0, dur: 1.5, n: 'F5' },
        { bar: 5, beat: 1.5, dur: 0.5, n: 'E5' },
        { bar: 5, beat: 2, dur: 2.0, n: 'D5' },

        // Bar 6 (F)
        { bar: 6, beat: 0, dur: 1.0, n: 'E5' },
        { bar: 6, beat: 1, dur: 1.0, n: 'F5' },
        { bar: 6, beat: 2, dur: 1.0, n: 'A5' },
        { bar: 6, beat: 3, dur: 1.0, n: 'B5' },

        // Bar 7 (C - resolution)
        { bar: 7, beat: 0, dur: 3.5, n: 'C6' },

        // --- SECOND HALF: Playful variation & high energy ---
        // Bar 8 (Am)
        { bar: 8, beat: 0, dur: 1.0, n: 'C6' },
        { bar: 8, beat: 1, dur: 0.5, n: 'B5' },
        { bar: 8, beat: 1.5, dur: 0.5, n: 'A5' },
        { bar: 8, beat: 2, dur: 1.5, n: 'E5' },
        { bar: 8, beat: 3.5, dur: 0.5, n: 'G5' },

        // Bar 9 (Em)
        { bar: 9, beat: 0, dur: 2.0, n: 'G5' },
        { bar: 9, beat: 2, dur: 2.0, n: 'E5' },

        // Bar 10 (F)
        { bar: 10, beat: 0, dur: 1.0, n: 'F5' },
        { bar: 10, beat: 1, dur: 1.0, n: 'A5' },
        { bar: 10, beat: 2, dur: 1.0, n: 'C6' },
        { bar: 10, beat: 3, dur: 1.0, n: 'D6' },

        // Bar 11 (C)
        { bar: 11, beat: 0, dur: 2.0, n: 'E6' },
        { bar: 11, beat: 2, dur: 2.0, n: 'C6' },

        // Bar 12 (Dm)
        { bar: 12, beat: 0, dur: 1.0, n: 'D6' },
        { bar: 12, beat: 1, dur: 1.0, n: 'C6' },
        { bar: 12, beat: 2, dur: 1.0, n: 'B5' },
        { bar: 12, beat: 3, dur: 1.0, n: 'A5' },

        // Bar 13 (G)
        { bar: 13, beat: 0, dur: 1.5, n: 'G5' },
        { bar: 13, beat: 1.5, dur: 0.5, n: 'A5' },
        { bar: 13, beat: 2, dur: 2.0, n: 'B5' },

        // Bar 14 (F)
        { bar: 14, beat: 0, dur: 1.0, n: 'C6' },
        { bar: 14, beat: 1, dur: 1.0, n: 'D6' },
        { bar: 14, beat: 2, dur: 1.0, n: 'E6' },
        { bar: 14, beat: 3, dur: 1.0, n: 'D6' },

        // Bar 15 (G - lead back into C for seamless loop)
        { bar: 15, beat: 0, dur: 2.0, n: 'C6' },
        { bar: 15, beat: 2, dur: 2.0, n: 'B5' }
    ];

    for (const m of menuLead) {
        addWhistleNote(left, right, m.bar * barSec + m.beat * beatSec, m.dur * beatSec, m.n, 0.44, 0.0);
    }

    saveStereoAudio('music_menu', left, right, true);
}

// =========================================================================
// 2. BOARD SCREEN ACT 1 MUSIC: "The Verdant Expedition"
// Distinct, open, adventurous Celtic expedition theme
// 124 BPM, Key: D Major / G Major, 16 bars (~30.96s seamless loop)
// =========================================================================
console.log('\nGenerating Track 2: Distinct & Adventurous Board Theme (music_act1)...');
{
    const bpm = 124;
    const beatSec = 60 / bpm;
    const barSec = beatSec * 4;
    const totalBars = 16;
    const totalSec = totalBars * barSec;
    const totalSamples = Math.round(totalSec * SAMPLE_RATE);

    const left = new Float32Array(totalSamples);
    const right = new Float32Array(totalSamples);

    // D Major / G Major Celtic Adventure Progression
    const boardChords = [
        { bar: 0, notes: ['D3', 'A3', 'D4', 'F#4', 'A4', 'D5'], bass: 'D3' },
        { bar: 1, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 2, notes: ['A2', 'E3', 'A3', 'C#4', 'E4', 'A4'], bass: 'A2' },
        { bar: 3, notes: ['D3', 'A3', 'D4', 'F#4', 'A4', 'D5'], bass: 'D3' },

        { bar: 4, notes: ['B2', 'F#3', 'B3', 'D4', 'F#4', 'B4'], bass: 'B2' },
        { bar: 5, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 6, notes: ['E2', 'B2', 'E3', 'G3', 'B3', 'E4'], bass: 'E2' },
        { bar: 7, notes: ['A2', 'E3', 'A3', 'C#4', 'E4', 'A4'], bass: 'A2' },

        { bar: 8, notes: ['D3', 'A3', 'D4', 'F#4', 'A4', 'D5'], bass: 'D3' },
        { bar: 9, notes: ['F#2', 'C#3', 'F#3', 'A3', 'C#4', 'F#4'], bass: 'F#2' },
        { bar: 10, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 11, notes: ['A2', 'E3', 'A3', 'C#4', 'E4', 'A4'], bass: 'A2' },

        { bar: 12, notes: ['B2', 'F#3', 'B3', 'D4', 'F#4', 'B4'], bass: 'B2' },
        { bar: 13, notes: ['G2', 'D3', 'G3', 'B3', 'D4', 'G4'], bass: 'G2' },
        { bar: 14, notes: ['A2', 'E3', 'A3', 'C#4', 'E4', 'A4'], bass: 'A2' },
        { bar: 15, notes: ['D3', 'A3', 'D4', 'F#4', 'A4', 'D5'], bass: 'D3' }
    ];

    // 2.1 Cascading Celtic Harp & Mandolin Plucking
    for (const c of boardChords) {
        const barStart = c.bar * barSec;
        // 8th-note harp arpeggio cascades (giving that magical woodland feel)
        for (let i = 0; i < 8; i++) {
            const noteIdx = (i % (c.notes.length - 1)) + 1;
            const harpFreq = noteFreq(c.notes[noteIdx]);
            const harpString = renderKarplusString(harpFreq, beatSec * 0.75, 0.994);
            const startIdx = Math.round((barStart + i * (beatSec * 0.5)) * SAMPLE_RATE);

            // Shimmering pan back and forth
            const pan = Math.sin(i * 1.2) * 0.45;
            const lG = Math.cos((pan + 1) * Math.PI / 4) * 0.22;
            const rG = Math.sin((pan + 1) * Math.PI / 4) * 0.22;

            for (let j = 0; j < harpString.length; j++) {
                const pos = startIdx + j;
                if (pos >= left.length) break;
                left[pos] += harpString[j] * lG;
                right[pos] += harpString[j] * rG;
            }
        }

        // Melodic Bassline
        addUprightBass(left, right, barStart + 0 * beatSec, beatSec * 0.9, c.bass, 0.50);
        addUprightBass(left, right, barStart + 1.5 * beatSec, beatSec * 0.5, c.notes[1], 0.38);
        addUprightBass(left, right, barStart + 2 * beatSec, beatSec * 0.9, c.bass, 0.45);
        addUprightBass(left, right, barStart + 3 * beatSec, beatSec * 0.9, c.notes[2], 0.42);
    }

    // 2.2 Rolling Celtic Bodhrán & Marching Beat
    for (let b = 0; b < totalBars * 4; b++) {
        const t = b * beatSec;
        // Bodhran galloping pulse
        if (b % 4 === 0 || b % 4 === 2) {
            addTavernPercussion(left, right, t, 'bodhran', 0.52);
        }
        if (b % 4 === 1 || b % 4 === 3) {
            addTavernPercussion(left, right, t, 'clap', 0.38);
        }
        // Celtic 8th-note gallop
        addTavernPercussion(left, right, t + beatSec * 0.5, 'tambourine', 0.22);
    }

    // 2.3 Expansive & Heroic High Pipe Melody (Completely distinct from Menu!)
    const boardLead = [
        // Bar 0 (D)
        { bar: 0, beat: 0, dur: 1.5, n: 'A5' },
        { bar: 0, beat: 1.5, dur: 0.5, n: 'F#5' },
        { bar: 0, beat: 2, dur: 1.0, n: 'D5' },
        { bar: 0, beat: 3, dur: 1.0, n: 'F#5' },

        // Bar 1 (G)
        { bar: 1, beat: 0, dur: 2.0, n: 'B5' },
        { bar: 1, beat: 2, dur: 1.5, n: 'A5' },
        { bar: 1, beat: 3.5, dur: 0.5, n: 'G5' },

        // Bar 2 (A)
        { bar: 2, beat: 0, dur: 1.5, n: 'F#5' },
        { bar: 2, beat: 1.5, dur: 0.5, n: 'E5' },
        { bar: 2, beat: 2, dur: 2.0, n: 'E5' },

        // Bar 3 (D)
        { bar: 3, beat: 0, dur: 3.5, n: 'F#5' },

        // Bar 4 (Bm)
        { bar: 4, beat: 0, dur: 1.5, n: 'D6' },
        { bar: 4, beat: 1.5, dur: 0.5, n: 'C#6' },
        { bar: 4, beat: 2, dur: 1.0, n: 'B5' },
        { bar: 4, beat: 3, dur: 1.0, n: 'A5' },

        // Bar 5 (G)
        { bar: 5, beat: 0, dur: 2.0, n: 'B5' },
        { bar: 5, beat: 2, dur: 2.0, n: 'G5' },

        // Bar 6 (Em)
        { bar: 6, beat: 0, dur: 1.5, n: 'A5' },
        { bar: 6, beat: 1.5, dur: 0.5, n: 'B5' },
        { bar: 6, beat: 2, dur: 1.0, n: 'A5' },
        { bar: 6, beat: 3, dur: 1.0, n: 'G5' },

        // Bar 7 (A)
        { bar: 7, beat: 0, dur: 3.5, n: 'A5' },

        // --- SECOND HALF: Soaring Climax ---
        // Bar 8 (D)
        { bar: 8, beat: 0, dur: 1.5, n: 'F#6' },
        { bar: 8, beat: 1.5, dur: 0.5, n: 'E6' },
        { bar: 8, beat: 2, dur: 2.0, n: 'D6' },

        // Bar 9 (F#m)
        { bar: 9, beat: 0, dur: 2.0, n: 'C#6' },
        { bar: 9, beat: 2, dur: 2.0, n: 'A5' },

        // Bar 10 (G)
        { bar: 10, beat: 0, dur: 1.0, n: 'B5' },
        { bar: 10, beat: 1, dur: 1.0, n: 'D6' },
        { bar: 10, beat: 2, dur: 2.0, n: 'E6' },

        // Bar 11 (A)
        { bar: 11, beat: 0, dur: 3.5, n: 'F#6' },

        // Bar 12 (Bm)
        { bar: 12, beat: 0, dur: 1.5, n: 'D6' },
        { bar: 12, beat: 1.5, dur: 0.5, n: 'E6' },
        { bar: 12, beat: 2, dur: 1.5, n: 'F#6' },
        { bar: 12, beat: 3.5, dur: 0.5, n: 'G6' },

        // Bar 13 (G)
        { bar: 13, beat: 0, dur: 2.0, n: 'E6' },
        { bar: 13, beat: 2, dur: 2.0, n: 'D6' },

        // Bar 14 (A)
        { bar: 14, beat: 0, dur: 1.5, n: 'C#6' },
        { bar: 14, beat: 1.5, dur: 0.5, n: 'B5' },
        { bar: 14, beat: 2, dur: 1.0, n: 'C#6' },
        { bar: 14, beat: 3, dur: 1.0, n: 'E6' },

        // Bar 15 (D - resolution for loop)
        { bar: 15, beat: 0, dur: 3.5, n: 'D6' }
    ];

    for (const m of boardLead) {
        addWhistleNote(left, right, m.bar * barSec + m.beat * beatSec, m.dur * beatSec, m.n, 0.45, 0.0);
    }

    saveStereoAudio('music_act1', left, right, true);
}

console.log('=== Both Tracks Successfully Generated with Stereo Depth and Warmth! ===');
