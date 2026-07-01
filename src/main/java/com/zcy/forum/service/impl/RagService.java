package com.zcy.forum.service.impl;

import com.github.houbb.heaven.util.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

    @Autowired
    private  VectorStore vectorStore;
    @Autowired
    private  TokenTextSplitter textSplitter;
    @Autowired
    private  ChatClient chatClient;



    // ====================== 1. 文档向量化入库 ======================
    public void embedDocumentToVectorStore(String filePath) {
        // 1. 读取本地文档（支持 txt/md/csv 等）
        Resource resource = new DefaultResourceLoader().getResource(filePath);
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.read();

        // 2. 文本分块
        List<Document> splitDocuments = textSplitter.split(documents);

        // 3. 向量化并保存到 PGVector
        vectorStore.add(splitDocuments);
        log.info("文档向量化入库完成，共 " + splitDocuments.size() + " 块");
    }

    // ====================== 2. 检索相似文档 ======================
    public List<String> searchSimilarDocuments(String query, int topK) {
        // 从 PGVector 检索最相似的 topK 条
        SearchRequest request = SearchRequest.builder().topK(topK)
                .query(query)
                .similarityThreshold(0.2)
                .build();
        List<Document> similarDocs = vectorStore.similaritySearch(request);

        // 提取文本内容
        return Optional.ofNullable(similarDocs)  // 先包集合，防 null
                .map(docs -> docs.stream()      // 不为 null 才执行流
                        .map(Document::getText) // 新版用 getContent
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList()); // 为 null 就返回空集合
    }

    // ====================== 3. RAG 问答（检索 + 大模型） ======================
    public String ragChat(String userQuestion) {
        // 1. 检索相关知识
        List<String> contextList = searchSimilarDocuments(userQuestion, 4);
        String context = String.join("\n", contextList);

        // 2. 构造 RAG 提示词
        String prompt = """
                你是一个专业的问答助手，请仅根据提供的上下文回答问题。
                上下文：
                %s
                
                用户问题：%s
                
                回答：
                """.formatted(context,userQuestion);
        // 3. 调用大模型生成回答
        return chatClient.prompt()
                .system(prompt)
                .user(userQuestion)
                .call()
                .content();
    }
}