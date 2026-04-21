package org.nembx.app.module.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.whitemagic2014.tts.TTS;
import io.github.whitemagic2014.tts.TTSVoice;
import io.github.whitemagic2014.tts.bean.Voice;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 直接走 HTTP 调用 SiliconFlow（OpenAI 兼容）transcription 接口。
 * 绕开 Spring AI 2.0.0-M1 在 Spring 7 下的 multipart 编码兼容问题。
 * 手工构造 multipart body，字节级对齐 curl -F 的 wire format：
 * - 文本字段 (model) 不带 Content-Type
 * - 文件字段根据扩展名带正确的 audio/* Content-Type
 *
 * @author Lian
 */
@Service
@Slf4j
public class AudioService {
    private static final String TRANSCRIPTION_PATH = "/v1/audio/transcriptions";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public AudioService(
            @Value("${app.stt.base-url}") String baseUrl,
            @Value("${app.stt.api-key}") String apiKey,
            @Value("${app.stt.model}") String model) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String speechToText(Resource audioFile) {
        long start = System.currentTimeMillis();
        try {
            String boundary = "---------springAiInterview" + UUID.randomUUID().toString().replace("-", "");
            byte[] body = buildMultipartBody(audioFile, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + TRANSCRIPTION_PATH))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() / 100 != 2) {
                log.error("[STT调用失败] status: {}, body: {}", response.statusCode(), response.body());
                throw new BusinessException(ErrorCode.AI_CALL_ERROR,
                        "STT失败 [" + response.statusCode() + "]: " + response.body());
            }

            JsonNode node = OBJECT_MAPPER.readTree(response.body());
            String text = node.path("text").asText("");
            log.info("[STT完成] 耗时: {}ms, 识别文本: {}",
                    System.currentTimeMillis() - start, text);
            return text;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[STT调用异常]", e);
            throw new BusinessException(ErrorCode.AI_CALL_ERROR, "STT调用失败: " + e.getMessage());
        }
    }

    public byte[] textToSpeech(String aiReply) {
        if (aiReply == null || aiReply.isBlank()) {
            throw new BusinessException(ErrorCode.AI_CALL_ERROR, "TTS输入文本为空");
        }

        long start = System.currentTimeMillis();
        String voiceName = "zh-CN-XiaoyiNeural";
        Voice voice = TTSVoice.provides().stream()
                .filter(v -> voiceName.equals(v.getShortName()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_CALL_ERROR, "TTS voice not found: " + voiceName));

        try {
            Path tmpDir = Files.createTempDirectory("tts-");
            String filename = "tts-" + UUID.randomUUID();
            try {
                new TTS(voice, aiReply)
                        .findHeadHook()
                        .isRateLimited(true)
                        .fileName(filename)
                        .overwrite(true)
                        .storage(tmpDir.toString())
                        .formatMp3()
                        .trans();

                Path mp3 = tmpDir.resolve(filename + ".mp3");
                if (!Files.exists(mp3) || Files.size(mp3) == 0) {
                    throw new BusinessException(ErrorCode.AI_CALL_ERROR, "TTS返回空音频");
                }
                byte[] bytes = Files.readAllBytes(mp3);
                log.info("[TTS完成] 耗时: {}ms, 字节数: {}", System.currentTimeMillis() - start, bytes.length);
                return bytes;
            } finally {
                deleteRecursive(tmpDir);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TTS调用异常]", e);
            throw new BusinessException(ErrorCode.AI_CALL_ERROR, "TTS调用失败: " + e.getMessage());
        }
    }

    private byte[] buildMultipartBody(Resource audioFile, String boundary) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] dashBoundaryCrlf = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);

        // model 字段（纯文本，不带 Content-Type，跟 curl 一致）
        os.write(dashBoundaryCrlf);
        os.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        os.write(model.getBytes(StandardCharsets.UTF_8));
        os.write(crlf);

        // file 字段
        String filename = audioFile.getFilename();
        if (filename == null || filename.isBlank()) {
            filename = "audio.mp3";
        }
        String fileContentType = guessAudioContentType(filename);
        os.write(dashBoundaryCrlf);
        os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Type: " + fileContentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        try (InputStream in = audioFile.getInputStream()) {
            in.transferTo(os);
        }
        os.write(crlf);

        // 结尾 boundary
        os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return os.toByteArray();
    }

    private String guessAudioContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".mp3") || lower.endsWith(".mpga") || lower.endsWith(".mpeg")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".webm")) return "audio/webm";
        if (lower.endsWith(".m4a") || lower.endsWith(".mp4")) return "audio/mp4";
        if (lower.endsWith(".ogg") || lower.endsWith(".opus")) return "audio/ogg";
        if (lower.endsWith(".flac")) return "audio/flac";
        return "application/octet-stream";
    }

    private void deleteRecursive(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
