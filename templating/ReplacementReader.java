package de.danielhons.lib.templating;

import java.util.Map;

public interface ReplacementReader<T> {
    Map<String,T> readReplacements(Object o);
}
