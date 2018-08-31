package de.danielhons.lib.annotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StringReplacementReader implements ReplacementReader<String> {

    @Override
    public Map<String, String> readReplacements(Object o) {
        Map<String, String> replacements = new HashMap<>();
        addToReplacements(o,"", replacements);
        return replacements;
    }



    private void addToReplacements(Object obj, String pathIdentifier, Map<String, String> replacements) {
        if (obj==null) return;
        if (pathIdentifier.length() > 0 && !pathIdentifier.endsWith(".")) pathIdentifier += ".";
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Replaces annonation = field.getAnnotation(Replaces.class);
                if (annonation != null) {
                    if (annonation.type().equals(Replaces.Type.STRING)) {
                        Object o = field.get(obj);
                        Template inner = o.getClass().getDeclaredAnnotation(Template.class);
                        if(inner!=null){ //Hat ein eigenes Template
                            replacements.put(pathIdentifier + field.getAnnotation(Replaces.class).value(),
                                             new TemplateParser().parse(o));
                        }
                        else { //Hat kein eigenes Template
                            replacements.put(pathIdentifier + field.getAnnotation(Replaces.class).value(),
                                             o.toString());
                        }
                    }
                    else if (annonation.type().equals(Replaces.Type.OBJECT)) {
                        addAllWithPrefix(replacements, readReplacements(field.get(obj)),
                                         pathIdentifier + annonation.value());

                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private String addDot(String pathIdentifier){
        if (pathIdentifier.length() > 0 && !pathIdentifier.endsWith(".")) pathIdentifier += ".";
        return pathIdentifier;
    }

    private void addAllWithPrefix(Map<String, String> main, Map<String, String> sub, String prefix) {
        sub.forEach((k, v) -> main.put(addDot(prefix) + k, v));
    }
}
