package com.dhieu.converter.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TsToJavaConverter {
    public static String convert(String tsCode) {
        Matcher interfaceMatcher = Pattern.compile("export\\s+interface\\s+(I?\\w+)\\s*\\{([\\s\\S]*?)\\}").matcher(tsCode);
        if (!interfaceMatcher.find()) {
            throw new RuntimeException("Not found interface");
        }

        String interfaceName = interfaceMatcher.group(1);
        String className = interfaceName.replaceFirst("^I", "") + "DTO";
        String body = interfaceMatcher.group(2);

        List<String> fields = new ArrayList<>();
        boolean usesBigDecimal = false;
        boolean usesList = false;

        for (String line : body.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || !line.contains(":")) continue;

            Matcher fieldMatcher = Pattern.compile("(\\w+)\\s*:\\s*([^;]+);?").matcher(line);
            if (!fieldMatcher.find()) continue;

            String name = fieldMatcher.group(1);
            String tsType = fieldMatcher.group(2).trim();
            String javaType;

            if (tsType.endsWith("[]")) {
                String innerTsType = tsType.substring(0, tsType.length() - 2).trim();
                String innerJavaType = mapType(innerTsType);
                javaType = "List<" + innerJavaType + ">";
                usesList = true;
                if (innerJavaType.equals("BigDecimal")) usesBigDecimal = true;
            } else {
                javaType = mapType(tsType);
                if (javaType.equals("BigDecimal")) usesBigDecimal = true;
            }

            fields.add("    private " + javaType + " " + name + ";");
        }

        StringBuilder sb = new StringBuilder();
        if (usesBigDecimal) sb.append("import java.math.BigDecimal;\n");
        if (usesList) sb.append("import java.util.List;\n");
        if (usesBigDecimal || usesList) sb.append("\n");

        sb.append("public class ").append(className).append(" {\n");
        for (String f : fields) sb.append(f).append("\n");
        sb.append("}");

        return sb.toString();
    }

    private static String mapType(String tsType) {
        return switch (tsType) {
            case "number" -> "BigDecimal";
            case "string" -> "String";
            case "boolean" -> "Boolean";
            case "Date" -> "Date";
            default -> "Object";
        };
    }
}
