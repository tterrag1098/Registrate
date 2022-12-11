package com.tterrag.registrate.test.meta;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Scrapes all protected methods from pasted source, and emits them as public super-calling stubs. Used to create the bouncer classes such as BuilderModelProvider.
 */
public class ProtectedMethodScraper {

    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Header {

        String className;
        boolean isStatic;
        String generics;
        String type;
        String name;
        String[] paramTypes;
        String[] paramNames;
        
        public Header applyTypeReplacement(Pair<String, String> repl) {
            String[] newParamTypes = Arrays.copyOf(paramTypes, paramTypes.length);
            for (int i = 0; i < newParamTypes.length; i++) {
                if (newParamTypes[i].equals(repl.getKey())) {
                    newParamTypes[i] = repl.getValue();
                }
            }
            String type = this.type.equals(repl.getKey()) ? repl.getValue() : this.type;
            return new Header(this.className, this.isStatic, this.generics, type, this.name, newParamTypes, this.paramNames);
        }

        public String printStubMethod(Class<?> source) {
            StringBuilder base = new StringBuilder();
            base.append("/** Generated override to expose protected method: {@link ").append(className).append(isStatic ? "." : "#").append(name).append("} */\n");
            if (!isStatic) {
                base.append("@Override\n");
            }
            base.append("@Generated(value = \"").append(source.getName()).append("\", date = \"")
                .append(DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now().atZone(ZoneOffset.UTC))).append("\")\n");
            base.append("public ").append(isStatic ? "static " : "");
            if (generics != null) {
                base.append(generics).append(" ");
            }
            base.append(type).append(" ").append(name).append("(");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    base.append(", ");
                }
                base.append(paramTypes[i]).append(" ").append(paramNames[i]);
            }
            base.append(") { ").append(type.equals("void") ? "" : "return ");
            if (isStatic) {
                base.append(className).append(".");
            } else {
                base.append("super.");
            }
            base.append(name).append("(").append(Arrays.stream(paramNames).collect(Collectors.joining(", "))).append("); }");
            return base.toString();
        }

        //                                                                                           Match generics up to three levels deep -- java does not support recursive patterns
        private static final Pattern HEADER_PATTERN = Pattern.compile("^\\s*protected\\s+(?:(static)\\s)?\\s*(<[^<>]+(?:<[^<>]+(?:<[^<>]+>[^<>]*)*>[^<>]*)*>)?\\s*(\\S+)\\s+(\\S+)\\((.+)\\)\\s\\{$");
        private static final Pattern PARAM_PATTERN = Pattern.compile("([a-zA-Z_][\\w.$]+(?:<.+>)?)\\s+(\\S+)");

        public static Optional<Header> parse(String className, String code) {
            Matcher h = HEADER_PATTERN.matcher(code);
            if (h.matches()) {
                boolean isStatic = h.group(1) != null;
                String generics = h.group(2);
                String type = h.group(3);
                String name = h.group(4);
                String params = h.group(5);
                String[] paramList = params.split(",");
                String[] paramTypes = new String[paramList.length];
                String[] paramNames = new String[paramList.length];
                for (int i = 0; i < paramList.length; i++) {
                    String param = paramList[i].trim();
                    Matcher p = PARAM_PATTERN.matcher(param);
                    if (p.matches()) {
                        paramTypes[i] = p.group(1);
                        paramNames[i] = p.group(2);
                    } else {
                        return Optional.empty();
                    }
                }
                return Optional.of(new Header(className, isStatic, generics, type, name, paramTypes, paramNames));
            }
            return Optional.empty();
        }
    }

    public static List<Header> scrapeInput() {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        System.out.println("Paste class source:");
        String line = scanner.nextLine();
        List<Header> headers = new ArrayList<>();
        String className = null;
        do {
            if (className != null) {
                Header.parse(className, line).ifPresent(headers::add);
            } else if (line.startsWith("public class ") || line.startsWith("public abstract class")) {
                className = line.replaceAll("public (abstract )?class ", "").split("\\s+")[0];
            }
            line = scanner.nextLine();
        } while (!"done".equals(line));
        return headers;
    }
}
