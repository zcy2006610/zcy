package com.zcy.forum.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Bean
    @Primary
    public VectorStore myVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate,embeddingModel)
                .dimensions(1024)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new DashScopeEmbeddingModel(
                new DashScopeApi(apiKey),
                MetadataMode.EMBED,
                DashScopeEmbeddingOptions.builder().withModel("text-embedding-v1").build());
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(512)          // 每个文本块大小（推荐 512，适配1024维向量）
                .withKeepSeparator(true)     // 保留分隔符（更通顺）
                .withMinChunkLengthToEmbed(10) // 小于这个长度的块不嵌入
                .build();
    }
}
