const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const SAMPLE_RATE = 44100;

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
        // Clean up temporary wav for space efficiency if ogg succeeded
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

module.exports = { SAMPLE_RATE, makeWav, saveAudio };
