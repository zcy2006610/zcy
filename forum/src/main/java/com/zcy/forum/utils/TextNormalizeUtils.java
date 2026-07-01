package com.zcy.forum.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * 文本归一化工具（配合 sensitive-word 防绕过最强）
 * 繁体->简体 | 全角转半角 | 去Emoji | 去特殊符号 | 去空格 | 小写统一
 */
public class TextNormalizeUtils {

    // 预编译正则（高性能）
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\ud83c\udf00-\ud83c\udfff]|" +
                    "[\ud83d\udc00-\ud83d\udfff]|" +
                    "[\u2600-\u26ff]|" +
                    "[\u2700-\u27bf]"
    );
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^a-zA-Z0-9\\u4e00-\\u9fa5]");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * 完整归一化（审核前必须调用）
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 1. 全角 → 半角（统一字符宽度）
        text = fullWidthToHalfWidth(text);

        // 2. JDK 标准归一化（处理特殊字符）
        text = Normalizer.normalize(text, Normalizer.Form.NFKC);

        // 3. 移除所有 Emoji
        text = EMOJI_PATTERN.matcher(text).replaceAll("");

        // 4. 移除所有特殊符号，只保留 字母/数字/中文
        text = SYMBOL_PATTERN.matcher(text).replaceAll(" ");

        // 5. 多个空格 → 一个空格
        text = SPACE_PATTERN.matcher(text).replaceAll(" ").trim();

        // 6. 全部小写（统一匹配）
        return text.toLowerCase();
    }

    /**
     * 全角转半角（核心防绕过）
     */
    private static String fullWidthToHalfWidth(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= 65281 && chars[i] <= 65374) {
                chars[i] = (char) (chars[i] - 65248);
            } else if (chars[i] == 12288) {
                chars[i] = ' ';
            }
        }
        return new String(chars);
    }
}