package com.zcy.forum;

import cn.hutool.json.JSONUtil;
import com.zcy.forum.service.PostService;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class ForumApplicationTests {

    @Autowired
    private PostService postService;

    @Autowired
    private RestTemplate restTemplate;
    @Test
    void contextLoads() throws URISyntaxException {
        String position="http://localhost:80/ai/service/chat";
        URI uri = new URI(position);
        String rule= """
                你是一个专业、严格、中立的论坛内容审核助手。
                你的任务是：对用户提交的帖子内容进行合规性审核，只输出审核结果，不做任何解释、不回答问题、不创作内容.
                审核规则
                1. 禁止内容（直接判定违规）
                政治敏感、违法违规、危害国家安全的内容
                色情、低俗、暴力、血腥、恐怖内容
                辱骂、人身攻击、引战、网暴言论
                广告、引流、二维码、联系方式、外部链接
                诈骗、赌博、毒品、盗版、破解相关内容
                歧视、仇恨、煽动对立言论
                明显灌水、无意义刷屏内容
                                
                2. 允许内容（判定正常）
                技术交流、学习提问、经验分享
                正常生活讨论、心情分享
                合规合规的求助、讨论、观点交流
                正常代码、知识点、教程内容
                             
               输出格式（必须严格遵守只输出yes或者no）
                内容合规：yes
                内容违规：no "}
                              
                不要输出任何多余文字、解释、语气词，只返回yes或者no。
                审核内容:{}
                """;
        String content = postService.query().eq("id",33L).one().getContent();
        System.out.println("待审核内容："+"||||" + content+"||||");
        String auditPrompt = rule.formatted(content);
        AiRequest aiRequest = new AiRequest();
        aiRequest.setContent(auditPrompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        HttpEntity<String> requestEntity = new HttpEntity<>(
                JSONUtil.toJsonStr(aiRequest), // 正确的 JSON 请求体
                headers
        );
        String result = restTemplate.postForObject(uri, requestEntity, String.class);
        System.out.println("审核结果: " + result);
    }


    @Data
    static class AiRequest {
        private String content;

    }

}
