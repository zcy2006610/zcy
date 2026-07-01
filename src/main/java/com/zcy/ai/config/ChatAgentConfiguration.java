package com.zcy.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.toolcalllimit.ToolCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolerror.ToolErrorInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.zcy.ai.properties.ChatAgentProperties;
import com.zcy.ai.properties.TavilySearchProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({ChatAgentProperties.class, TavilySearchProperties.class})
public class ChatAgentConfiguration {

    /**
     * MySQL 对话状态持久化器。
     *
     * 作用：把 ReactAgent 每一步执行的 Checkpoint（对话状态快照）持久化到 MySQL。
     * 应用重启后能恢复到上次对话的上下文，实现"断点续聊"。
     *
     * createOption = CREATE_IF_NOT_EXISTS：首次启动时自动建表，无需手动 DDL。
     *
     * 对应 LangGraph 概念：checkpointer=MemorySaver() / SqliteSaver()
     */
    @Bean
    public MysqlSaver mysqlCheckpointSaver(DataSource dataSource) {

        return MysqlSaver.builder()
                .dataSource(dataSource)
                .createOption(CreateOption.CREATE_IF_NOT_EXISTS)
                .build();
    }

    /**
     * Tavily 联网搜索工具注册。
     *
     * 将 TavilySearchTool 这个 Java 类包装成 Agent 可调用的 Function Callback：
     *   - 工具名称 "tavily_search"：模型 function calling 时以此名称匹配工具
     *   - .description(...)：工具的 description 字段，直接写入 function calling 的 JSON Schema，
     *     告诉模型"什么场景下该调用这个工具"
     *   - .inputType(TavilySearchRequest.class)：框架自动反射生成 JSON Schema 的 parameters 定义
     *
     * 对应 LangGraph 概念：ToolNode([tool]) + 工具的 @tool 装饰器
     */
   /* @Bean
    public ToolCallback tavilySearchToolCallback(TavilySearchTool tavilySearchTool) {

        return FunctionToolCallback
                .builder("tavily_search", tavilySearchTool::search)
                .description("联网搜索最新信息、事实资料和网页来源。调用时必须传 JSON 参数，且至少包含非空 query；可选 topic 和 maxResults，其中 topic 仅允许 general、news、finance。")
                .inputType(TavilySearchRequest.class)
                .build();
    }*/

    /**
     * 核心 ReactAgent Bean 装配。
     *
     * 所有 Agent 行为在此通过 Builder 模式声明式组装，
     * build() 时框架内部自动生成 ReAct 图结构（model → tools → model 循环）。
     *
     * 配置分为六个层次：基础配置 → 工具与状态 → 并行执行 → Hooks 防死循环 → Interceptors 拦截器链
     */
    @Bean
    public ReactAgent businessChatReactAgent(ChatModel chatModel,
                                             MysqlSaver mysqlCheckpointSaver,
                                             ToolCallback tavilySearchToolCallback,
                                             ChatAgentProperties chatAgentProperties)
                                             //DashScopeCompatibilityInterceptor dashScopeCompatibilityInterceptor,
                                             //TavilyToolInputFallbackInterceptor tavilyToolInputFallbackInterceptor)
                                             {
        return ReactAgent.builder()

                // ==================== 第一层：基础配置 ====================
                // Agent 名称，用于日志和追踪标识
                .name("business_chat_agent")
                // 注入 ChatModel（通义千问 via DashScope / OpenAI 兼容协议）
                .model(chatModel)
                // System Prompt，定义 Agent 角色和行为规范（从 application.yml 的 app.chat.system-prompt 读取）
                .instruction(chatAgentProperties.getSystemPrompt())

                // ==================== 第二层：工具注册与对话状态持久化 ====================
                // 将 tavily_search 工具注册给 Agent，模型可自主决定何时调用
                .tools(tavilySearchToolCallback)
                // 绑定 MySQL Checkpoint 持久化，每一步执行状态写入数据库
                .saver(mysqlCheckpointSaver)

                // ==================== 第三层：并行工具执行 ====================
                // 允许并行调用工具（模型一次返回多个 tool call 时并发执行）
                .parallelToolExecution(true)
                // 最多同时执行 4 个工具，防止并发过高打垮外部 API
                .maxParallelTools(4)

                // ==================== 第四层：Hooks — 防死循环与资源保护 ====================
                .hooks(
                        // Hook 1：模型调用次数限制
                        // 防止 Agent 在 ReAct 循环中无限调用模型（反复 "思考 → 调工具" 停不下来）
                        ModelCallLimitHook.builder()
                                // runLimit：单次请求最多调用模型 8 次（默认值，可通过 app.chat.max-model-calls-per-run 覆盖）
                                .runLimit(chatAgentProperties.getMaxModelCallsPerRun())
                                // threadLimit：单个会话生命周期累计最多调用模型 40 次（默认值）
                                .threadLimit(chatAgentProperties.getMaxModelCallsPerThread())
                                // ExitBehavior.END：超限后优雅结束，返回已有结果，不抛异常
                                .exitBehavior(ModelCallLimitHook.ExitBehavior.END)
                                .build(),

                        // Hook 2：工具调用次数限制
                        // 只针对 tavily_search，防止反复换 query 搜同一个问题
                        ToolCallLimitHook.builder()
                                .toolName("tavily_search")
                                // runLimit：单次请求 tavily_search 最多调用 6 次（默认值）
                                .runLimit(chatAgentProperties.getMaxToolCallsPerRun())
                                // threadLimit：单个会话累计最多调用 30 次（默认值）
                                .threadLimit(chatAgentProperties.getMaxToolCallsPerThread())
                                // 超限后优雅结束
                                .exitBehavior(ToolCallLimitHook.ExitBehavior.END)
                                .build()
                )

                // ==================== 第五层：Interceptors 拦截器链 ====================
                // 拦截器按声明顺序构成一条链：请求依次经过每个拦截器，最后到达实际调用
                // 链顺序：DashScope 兼容 → Tavily 入参修复 → 重试 → 错误兜底
                .interceptors(
                        // Interceptor 1：DashScope 参数兼容（ModelInterceptor 子类）
                        // 阿里 DashScope API 兼容 OpenAI 格式，但不支持 parallel_tool_calls、stream_options 等参数。
                        // 在模型请求发出前自动剥离这些不兼容参数，避免 HTTP 400 错误。
                        //dashScopeCompatibilityInterceptor,

                        // Interceptor 2：Tavily 工具入参自动修复（ToolInterceptor 子类）
                        // 模型生成的 tool call arguments 可能不规范（空参数、缺 query 字段、纯文本非 JSON）。
                        // 该拦截器自动从上下文提取 query 并补充到 arguments 中，填写失败则返回错误提示。
                        //tavilyToolInputFallbackInterceptor,

                        // Interceptor 3：工具调用自动重试（ToolInterceptor 子类）
                        // 指数退避 + 随机抖动重试策略：
                        //   第 1 次失败 → 等待约 200ms → 第 2 次调用
                        //   第 2 次失败 → 等待约 400ms → 第 3 次调用（最后一次）
                        //   全部失败 → 返回错误消息（不抛异常，用户能看到部分结果）
                        ToolRetryInterceptor.builder()
                                .toolName("tavily_search")                                   // 只对 tavily_search 做重试
                                .maxRetries(2)                                               // 最多重试 2 次（共执行 3 次）
                                .initialDelay(200L)                                          // 首次重试延迟 200ms
                                .maxDelay(1200L)                                             // 最大延迟 1200ms
                                .jitter(true)                                                // 加随机抖动，避免惊群效应
                                .onFailure(ToolRetryInterceptor.OnFailureBehavior.RETURN_MESSAGE) // 最终失败返回消息而非抛异常
                                .build(),

                        // Interceptor 4：工具执行异常兜底（ToolInterceptor 子类）
                        // 捕获工具执行过程中的未预期异常（如 NPE、网络超时），
                        // 转换为结构化错误响应，避免原始异常堆栈直接暴露给用户。
                        ToolErrorInterceptor.builder().build()
                )

                // build() 时框架内部自动生成 ReAct 图结构：
                // model node → [should_continue?] → tools node → model node → ...
                .build();
    }
}
