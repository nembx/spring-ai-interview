package org.nembx.app.module.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lian
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {

    private final ChatClient.Builder chatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        return chatClient.build()
                .prompt(prompt)
                .system("you are a ai assistant")
                .call()
                .content();
    }
}
