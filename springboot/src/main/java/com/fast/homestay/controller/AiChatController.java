package com.fast.homestay.controller;


import com.fast.homestay.domain.Homestay;
import com.fast.homestay.service.HomestayService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LJH
 * @version 2.0
 */
@RestController
@RequestMapping("/homestay/chat")
public class AiChatController {

    // ChatClient 是 Spring AI 提供的"大模型客户端"
    // 所有跟大模型的交互都通过它来完成
    private final ChatClient chatClient;

    @Resource
    private HomestayService homestayService;

    public AiChatController(ChatClient.Builder builder) {
        // 关键：在 Prompt 中明确 AI 的职责和数据来源
        String SYSTEM_PROMPT = """
            你是一个商品推荐专家。
            请严格遵守以下规则：
            1. 仅能根据我提供的【商品数据】进行推荐，不要回答数据库之外的产品。
            2. 如果数据库中没有匹配的商品，礼貌告诉用户目前没有，不要编造。
            3. 回复请包含商品标题、价格和详细描述。
            4. 若用户输入模糊，请基于现有数据给出建议，不要发散。
            """;
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message,
                             @RequestParam(defaultValue = "default") String sessionId) {
        SseEmitter emitter = new SseEmitter(180_000L);

        // 1. 根据用户输入检索数据库
        List<Homestay> products = homestayService.searchHomestayForAI(message);

        // 2. 格式化数据，节省 Token 并提高 AI 理解能力
        String context = products.isEmpty() ?
                "抱歉，库存中暂无相关商品。" :
                products.stream()
                .map(p -> String.format("- 商品名称：%s，价格：%s 元，卖点：%s",
                        p.getTitle(), p.getPrice(), p.getDescription()))
                .collect(Collectors.joining("\n"));

        // 3. 将上下文注入 AI
        chatClient.prompt()
                .system(s -> s.param("context", "以下是可供推荐的商品清单：\n" + context +
                        "\n\n请你只根据上述清单中的商品进行回答，如果清单中没有，请引导用户联系客服，严禁胡编乱造。"))
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .subscribe(
                        token -> { try { emitter.send(token); } catch (Exception e) { emitter.completeWithError(e); } },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }
}
