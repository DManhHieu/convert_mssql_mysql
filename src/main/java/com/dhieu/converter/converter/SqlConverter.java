package com.dhieu.converter.converter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlConverter {

    private static final String[] SQL_KEYWORDS = {
        "SELECT", "FROM", "WHERE", "AS", "AND", "OR", "ON", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
        "GROUP", "BY", "HAVING", "ORDER", "DESC", "ASC", "TOP", "DISTINCT", "CASE", "WHEN", "THEN",
        "ELSE", "END", "IN", "IS", "NULL", "NOT", "BETWEEN", "LIKE", "EXISTS", "UNION", "ALL", "LIMIT"
    };

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b(?!\\d)\\w+\\b");

    private static final Pattern QUOTED = Pattern.compile("(\\[[^\\]]+\\]|\"[^\"]+\")");

    private static final Pattern CAMEL_CASE = Pattern.compile("(?<=[a-z])(?=[A-Z])");

    public static String convertMssqlToMysql(String mssqlQuery) {
        String result = mssqlQuery;
        result = replaceType(result);
        result = replaceSelectTop(result);
        result = replaceDBO(result.trim());
        result = convertBackquoteToSnakeCase(result);
        result = convertBracketsToSnakeCase(result);
        result = convertAliasShowcase(result);
        result = convertTableColumn(result);
        result = convertEomonth(result);
        result = convertAllIdentifiersToSnakeCase(result);
        return result;
    }

    private static String replaceType(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)GETDATE\\s*\\(\\)", "NOW()");
        result = result.replaceAll("(?i)ISNULL\\s*\\(", "IFNULL(");
        result = result.replaceAll("\\[([^\\]]+)\\]", "`$1`");
        result = result.replaceAll("(?i)NVARCHAR", "VARCHAR");
        result = result.replaceAll("(?i)DATETIME2", "DATETIME");
        result = result.replaceAll("(?i)BIT", "TINYINT(1)");
        result = result.replaceAll("(?i)\\bLEN\\s*\\(", "CHAR_LENGTH(");
        return result;
    }

    private static String replaceSelectTop(String sql) {
        String result = sql;
        Pattern topPattern = Pattern.compile("(?i)SELECT\\s+TOP\\s+(\\d+)\\s", Pattern.CASE_INSENSITIVE);
        Matcher matcher = topPattern.matcher(result);
        if (matcher.find()) {
            String topN = matcher.group(1);
            result = matcher.replaceFirst("SELECT ");
            result = result.replaceAll("(?i);?$", " LIMIT " + topN + ";");
        }

        return result.replaceAll("(?i)SELECT\\s+TOP\\s*\\(\\s*100\\s*\\)\\s*PERCENT", "SELECT");
    }

    private static String convertAliasShowcase(String sql) {
        Pattern aliasPattern = Pattern.compile("(?i)\\s+AS\\s+(\\w+)");
        Matcher aliasMatcher = aliasPattern.matcher(sql);
        StringBuilder sb = new StringBuilder();
        while (aliasMatcher.find()) {
            String alias = aliasMatcher.group(1).toLowerCase();
            aliasMatcher.appendReplacement(sb, " AS " + alias);
        }
        aliasMatcher.appendTail(sb);
        return sb.toString();
    }

    private static String convertTableColumn(String sql) {
        Pattern dotPattern = Pattern.compile("(\\w+)\\.(\\w+)");
        Matcher dotMatcher = dotPattern.matcher(sql);
        StringBuilder sb = new StringBuilder();
        while (dotMatcher.find()) {
            String table = dotMatcher.group(1).toLowerCase();
            String column = dotMatcher.group(2).toLowerCase();
            dotMatcher.appendReplacement(sb, table + "." + column);
        }
        dotMatcher.appendTail(sb);
        return sb.toString();
    }

    private static String replaceDBO(String sql) {
        return sql.replaceAll("(?i)\\[dbo]\\.", "")
            .replaceAll("dbo.","");
    }

    private static String convertBracketsToSnakeCase(String sql) {
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        return convertToSnakeCase(sql, pattern);
    }

    private static String convertBackquoteToSnakeCase(String sql) {
        Pattern pattern = Pattern.compile("`(.*?)`");
        return convertToSnakeCase(sql, pattern);
    }
    private static String convertToSnakeCase(String sql, Pattern pattern) {
        Matcher matcher = pattern.matcher(sql);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String original = matcher.group(1);
            String snakeCase = toSnakeCase(original);
            matcher.appendReplacement(result, Matcher.quoteReplacement(snakeCase));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String toSnakeCase(String input) {
        return input.trim()
            .replaceAll("[\\s\\-]+", "_")            // spaces/dashes → underscores
            .replaceAll("([a-z])([A-Z])", "$1_$2")   // camelCase → snake_case
            .replaceAll("[^a-zA-Z0-9_]", "")         // remove non-alphanumerics
            .toLowerCase();
    }
    public static String convertAllIdentifiersToSnakeCase(String sql) {
        Set<String> keywords = new HashSet<>();
        for (String kw : SQL_KEYWORDS) {
            keywords.add(kw.toUpperCase());
        }

        StringBuilder result = new StringBuilder();
        Matcher quotedMatcher = QUOTED.matcher(sql);

        int lastEnd = 0;
        while (quotedMatcher.find()) {
            // Convert non-quoted part
            String before = sql.substring(lastEnd, quotedMatcher.start());
            result.append(convertUnquotedPart(before, keywords));

            // Append quoted part as-is
            result.append(quotedMatcher.group());
            lastEnd = quotedMatcher.end();
        }

        // Convert the tail (after last quote)
        result.append(convertUnquotedPart(sql.substring(lastEnd), keywords));

        return result.toString();
    }

    private static String convertUnquotedPart(String input, Set<String> keywords) {
        Matcher wordMatcher = WORD_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder();

        while (wordMatcher.find()) {
            String word = wordMatcher.group();
            if (keywords.contains(word.toUpperCase())) {
                wordMatcher.appendReplacement(buffer, word); // keep keyword
            } else {
                String snake = toSnakeCase(word);
                wordMatcher.appendReplacement(buffer, snake);
            }
        }
        wordMatcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String camelCaseToSnakeCase(String word) {
        return CAMEL_CASE.matcher(word).replaceAll("_").toLowerCase();
    }

    private static String convertEomonth(String sql) {
        // Replace EOMONTH(date) → LAST_DAY(date)
        sql = sql.replaceAll("(?i)EOMONTH\\s*\\(([^,\\)]+)\\)", "LAST_DAY($1)");

        // Replace EOMONTH(date, offset) → LAST_DAY(DATE_ADD(date, INTERVAL offset MONTH))
        sql = sql.replaceAll("(?i)EOMONTH\\s*\\(([^,\\)]+)\\s*,\\s*([^\\)]+)\\)",
            "LAST_DAY(DATE_ADD($1, INTERVAL $2 MONTH))");

        return sql;
    }
}
