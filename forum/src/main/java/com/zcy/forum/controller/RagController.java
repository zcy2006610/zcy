package com.zcy.forum.controller;

import com.zcy.forum.annotation.RateLimit;
import com.zcy.forum.service.impl.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
@Tag(name = "rag检索相关功能")
public class RagController {

    @Autowired
    private RagService ragService;

    // 1. 文档入库（调用一次即可）
    @GetMapping("/embed")
    @Operation(summary = "文本向量化")
    public String embedDoc() {
        // 把你的文档放在 resources 下
        ragService.embedDocumentToVectorStore("classpath:doc/标签.txt");
        return "向量化完成";
    }

    // 2. 纯检索
    @GetMapping("/search")
    @Operation(summary = "文本检索")
    public Object search(@RequestParam String query) {
        return ragService.searchSimilarDocuments(query, 3);
    }

    // 3. RAG 问答
    @GetMapping("/rag")
    @RateLimit
    @Operation(summary = "rag检索问答")
    public String rag(@RequestParam String question) {
        return ragService.ragChat(question);
    }
}