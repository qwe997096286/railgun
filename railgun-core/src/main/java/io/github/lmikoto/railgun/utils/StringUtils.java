package io.github.lmikoto.railgun.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author liuyang
 * 2020/12/5 10:40 下午
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {


    public static final char UNDERLINE = '_';

    /**
     * 下划线字符串修改为驼峰命名
     */
    public static String underlineToCamel(String param) {
        return underlineToCamel(param, false);
    }

    /**
     * 下划线字符串修改为驼峰命名
     * @param firstUpper 首字符是否大写
     */
    public static String underlineToCamel(String param, boolean firstUpper) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                if (i == 0 && firstUpper) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 转化首字母大小
     * @param firstUpper 首字符是否大写
     */
    public static String camelToCamel(String param, boolean firstUpper) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        char c = param.charAt(0);
        if (Character.isUpperCase(c)) {
            if (firstUpper) {
                return param;
            } else {
                return param.replaceFirst(c + "", Character.toLowerCase(c) + "");
            }
        } else {
            if (firstUpper) {
                return param.replaceFirst(c + "", Character.toUpperCase(c) + "");
            } else {
                return param;
            }
        }
    }

    /**
     * 根据大写字母分隔
     * @param param 字符串
     */
    public static String camelToSub(String param, int begin, int end) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        StringBuilder lastWord = new StringBuilder();
        StringBuilder result = new StringBuilder();
        int j = 0;
        for(int i = 0; i < param.length() ; i++) {
            if (i != 0 && Character.isUpperCase(param.charAt(i))) {
                if (begin <= j && end >= j) {
                    result.append(lastWord);

                }
                lastWord = new StringBuilder();
                j++;
            }
            lastWord.append(param.charAt(i));
        }
        if (begin <= j && end >= j) {
            result.append(lastWord);

        }
        return result.toString();
    }

    /**
     * 驼峰名称修改为下划线分隔的字符串
     */
    public static String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.append("\n").append("BuildNumber:");
        return stringWriter.toString();
    }
}
