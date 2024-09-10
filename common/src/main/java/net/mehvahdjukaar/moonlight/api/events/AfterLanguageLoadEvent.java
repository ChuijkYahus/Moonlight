package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfterLanguageLoadEvent implements SimpleEvent {

    private final Map<String, String> languageLines;
    private final List<String> languageInfo;
    private final Map<String, String> extraLanguageLines = new HashMap<>();

    public AfterLanguageLoadEvent(Map<String, String> lines, List<String> info) {
        this.languageInfo = info;
        this.languageLines = lines;
    }

    @Nullable
    public String getEntry(String key) {
        return languageLines.get(key);
    }

    public void addEntry(String key, String translation) {
        languageLines.computeIfAbsent(key, k -> translation);
        extraLanguageLines.put(key, translation);
    }

    public void addEntries(LangBuilder builder) {
        builder.entries().forEach(this::addEntry);
    }

    public Map<String, String> getExtraLanguageLines() {
        return extraLanguageLines;
    }

    public boolean isDefault() {
        return languageInfo.stream().anyMatch(l -> l.equals("en_us"));
    }
}