package com.tterrag.registrate.test.meta;

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
            return new Header(this.className, this.isStatic, type, this.name, newParamTypes, this.paramNames);
        }

        public String printStubMethod() {
            StringBuilder base = new StringBuilder();
            if (!isStatic) {
                base.append("@Override\n");
            }
            base.append("public ").append(isStatic ? "static " : "").append(type).append(" ").append(name).append("(");
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

        private static final Pattern HEADER_PATTERN = Pattern.compile("^\\s+protected\\s(static)?\\s?((?:<[^>]+>\\s)?\\S+)\\s(\\S+)\\((.+)\\)\\s\\{$");
        private static final Pattern PARAM_PATTERN = Pattern.compile("([a-zA-Z_][\\w.$]+(?:<.+>)?)\\s+(\\S+)");

        public static Optional<Header> parse(String className, String code) {
            Matcher h = HEADER_PATTERN.matcher(code);
            if (h.matches()) {
                boolean isStatic = h.group(1) != null;
                String type = h.group(2);
                String name = h.group(3);
                String params = h.group(4);
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
                return Optional.of(new Header(className, isStatic, type, name, paramTypes, paramNames));
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
