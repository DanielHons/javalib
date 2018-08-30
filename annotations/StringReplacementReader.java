package de.danielhons.lib.annotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StringReplacementReader implements ReplacementReader<String> {

    private Map<String,String> replacements;

    @Override
    public Map<String, String> readReplacements(Object o) {
         replacements= new HashMap<>();
        addToReplacements(o);
        return replacements;
    }

    private void addToReplacements(Object obj) {
        addToReplacements(obj,"");
    }

    private void addToReplacements(Object obj, String prefix) {
        if (prefix.length()>0 && !prefix.endsWith(".")) prefix+=".";
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Replaces annonation =field.getAnnotation(Replaces.class);
                if (annonation!=null) {
                    if (annonation.type().equals(Replaces.Type.STRING))
                        replacements.put(prefix+field.getAnnotation(Replaces.class).value(), field.get
                                (obj).toString());
                    else if (annonation.type().equals(Replaces.Type.OBJECT)){
                        addToReplacements(field.get(obj),prefix+annonation.value());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
