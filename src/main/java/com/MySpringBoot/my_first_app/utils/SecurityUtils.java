package com.MySpringBoot.my_first_app.utils;

/**
 * 输入安全过滤工具类 — 防止 XSS 和注入攻击
 */
public class SecurityUtils {

    /**
     * 过滤 HTML 标签，防止 XSS 攻击
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#x27;")
            .replaceAll("/", "&#x2F;");
    }

    /**
     * 过滤图书描述等富文本（保留基本标点，去除 HTML/JS）
     */
    public static String sanitizeText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 去除 script 标签及内容
        String cleaned = input.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        // 去除 on 开头的 JS 事件属性
        cleaned = cleaned.replaceAll("(?i)\\s*on\\w+\\s*=\\s*\"[^\"]*\"", "");
        cleaned = cleaned.replaceAll("(?i)\\s*on\\w+\\s*=\\s*'[^']*'", "");
        // 去除 javascript: 伪协议
        cleaned = cleaned.replaceAll("(?i)javascript:", "");
        // 转义剩余 HTML
        return sanitize(cleaned);
    }

    /**
     * 验证密码强度
     */
    public static String validatePassword(String password) {
        if (password == null || password.length() < 6) {
            return "密码长度至少 6 位";
        }
        if (password.length() > 50) {
            return "密码长度不能超过 50 位";
        }
        return null; // null 表示验证通过
    }

    /**
     * 验证图书价格
     */
    public static String validatePrice(Double price) {
        if (price == null || price <= 0) {
            return "价格必须大于 0";
        }
        if (price > 99999) {
            return "价格不能超过 99999";
        }
        return null;
    }
}
