package org.nembx.app.module.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AudioService {
    private final TextToSpeechModel ttsModel;
    private final TranscriptionModel sttModel;

    /**
     * TTS: 文本 → 语音 (mp3 字节数组)
     */
    public byte[] textToSpeech(String text) {
        // 最简写法 — 直接传 String
        return ttsModel.call(text);
    }

    /**
     * STT: 语音 → 文本
     */
    public String speechToText(Resource audioFile) {
        // 最简写法
        return sttModel.transcribe(audioFile);
    }

    // 流式 TTS — 返回音频字节分块
    public Flux<byte[]> textToSpeechStream(String text) {
        return ttsModel.stream(text);
    }
}
