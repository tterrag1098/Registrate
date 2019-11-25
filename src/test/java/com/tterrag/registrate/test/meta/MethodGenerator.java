package com.tterrag.registrate.test.meta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.tterrag.registrate.test.meta.ProtectedMethodScraper.Header;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MethodGenerator {
    
    @Value
    private class Exclusion {

        String name;
        String[] params;
        
        public boolean matches(Header header) {
            if (!header.getName().equals(this.name)) {
                return false;
            }
            return this.params == null ? true : Arrays.equals(header.getParamTypes(), this.params);
        }
    }
    
    private static final String START_KEY = "// GENERATED START";
    private static final String END_KEY = "// GENERATED END";
    
    private final List<Pair<String, String>> typeReplacements;
    private final Set<Exclusion> excludes = new HashSet<>();
    
    public MethodGenerator() {
        this(ImmutableList.of());
    }
    
    public MethodGenerator exclude(String name) {
        excludes.add(new Exclusion(name, null));
        return this;
    }
    
    public MethodGenerator exclude(String name, String... paramTypes) {
        excludes.add(new Exclusion(name, paramTypes));
        return this;
    }

    public void generate(Path output) throws IOException {
        List<String> currentSource = Files.readAllLines(output, Charsets.UTF_8);
        List<Header> newHeaders = ProtectedMethodScraper.scrapeInput();
        ListIterator<Header> headerItr = newHeaders.listIterator();
        while (headerItr.hasNext()) {
            Header header = headerItr.next();
            for (Pair<String, String> repl : typeReplacements) {
                header = header.applyTypeReplacement(repl);
            }
            headerItr.set(header);
        }
        ListIterator<String> itr = currentSource.listIterator();
        while (itr.hasNext()) {
            int index = itr.nextIndex();
            String line = itr.next();
            if (line.contains(START_KEY)) {
                itr.remove();
                while (itr.hasNext()) {
                    if (!itr.next().contains(END_KEY)) {
                        itr.remove();
                    } else {
                        itr.remove();
                        itr = currentSource.listIterator(index);
                        break;
                    }
                }
                itr.add("    " + START_KEY);
                for (Header header : newHeaders) {
                    if (excludes.stream().anyMatch(e -> e.matches(header))) continue;
                    itr.add("");
                    for (String s : header.printStubMethod().split("\n")) {
                        itr.add("    " + s);
                    }
                }
                itr.add("");
                itr.add("    " + END_KEY);
                break;
            }
        }
        Files.write(output, currentSource);
    }
}
