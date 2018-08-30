package de.danielhons.lib.annotations;

import java.util.Map;

public interface ReplacementReader<T> {
    Map<String,T> readReplacements(Object o);
}
