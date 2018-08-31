package de.danielhons.lib.templating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static de.danielhons.lib.templating.Replaces.Type;

@Slf4j
public class StringReplacementReader implements ReplacementReader<String> {

    private TemplateParser parser=new TemplateParser();

    @Override
    public Map<String, String> readReplacements(Object o) {
        Map<String, String> replacements = new HashMap<>();
        addToReplacements(o, replacements);
        return replacements;
    }


    private void addToReplacements(Object obj, Map<String, String> replacements) {
        if (obj == null) return;
        processObject(obj, replacements);

    }

    private void processObject(Object obj, Map<String, String> replacements) {
        Class cls = obj.getClass();
        processClass(cls, obj, replacements);
        while ((cls = cls.getSuperclass()) != null) {
            processClass(cls, obj, replacements);
        }
    }

    private void processClass(Class cls, Object obj, Map<String, String> replacements) {
        processFields(cls, obj, replacements);
        processMethods(cls, obj, replacements);
    }


    private void processFields(Class cls, Object obj, Map<String, String> replacements) {
        Field[] fields = cls.getDeclaredFields();
        processFields(fields, obj, replacements);
    }

    private void processFields(Field[] fields, Object obj, Map<String, String> replacements) {
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                processReplacesOnField(field, obj, replacements);
                processConditionOnField(field, obj, replacements);
                processIterableOnField(field, obj, replacements);
            } catch (IllegalAccessException e) {
                log.error("Could not access field", e);
            }

        }
    }

    private void processIterableOnField(Field field, Object obj, Map<String, String> replacements)
            throws IllegalAccessException {
        ListReplaces annotation = field.getAnnotation(ListReplaces.class);
        if (annotation==null) return;
        Iterable list = (Iterable) field.get(obj);
        replaceIterable(annotation, list, replacements);
    }

    private void processConditionOnField(Field field, Object obj, Map<String, String> replacements)
            throws IllegalAccessException {
        ReplaceCondition annotation = field.getAnnotation(ReplaceCondition.class);
        if (annotation != null) {
            processConditionAnnotation(annotation, (Boolean) field.get(obj), replacements);
        }
    }

    private void processReplacesOnField(Field field, Object obj, Map<String, String> replacements)
            throws IllegalAccessException {
        Replaces annonation = field.getAnnotation(Replaces.class);
        if (annonation != null) {
            processReplacesAnnotation(annonation, field.get(obj), replacements);
        }

    }


    private void processMethods(Class cls, Object obj, Map<String, String> replacements) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            try {
                processReplaceOnMethod(method, obj, replacements);
                processConditionOnMethod(method, obj, replacements);
                processIterableOnMethod(method, obj, replacements);
            } catch (IllegalAccessException | InvocationTargetException e) {
                String msg = "Could not invoke Replacement for object, does it have arguments?";
                throw new RuntimeException(msg, e);
            }
        }
    }

    private void processIterableOnMethod(Method method, Object obj, Map<String, String> replacements)
            throws InvocationTargetException, IllegalAccessException {
        ListReplaces annotation = method.getAnnotation(ListReplaces.class);
        if (annotation==null) return;
        Iterable list = (Iterable) method.invoke(obj, (Object[]) null);
        replaceIterable(annotation, list, replacements);
    }


    private void replaceIterable(ListReplaces annotation, Iterable list, Map<String, String> replacements) {
        StringBuilder builder = new StringBuilder();
        for (Object o : list) {
            builder.append(parser.parse(o));
            builder.append(" "); //TODO parameter required?
        }
        replacements.put(annotation.value(), builder.toString());
    }

    private void processConditionOnMethod(Method method, Object obj, Map<String, String> replacements)
            throws InvocationTargetException, IllegalAccessException {
        ReplaceCondition annotation = method.getAnnotation(ReplaceCondition.class);
        if (annotation != null) {
            processConditionAnnotation(annotation, (Boolean) method.invoke(obj, (Object[]) null), replacements);
        }
    }

    private void processConditionAnnotation(ReplaceCondition annotation,
                                            Boolean value,
                                            Map<String, String> replacements) {
        if (value) { replacements.put(annotation.value(), annotation.ifTrue()); }
        else { replacements.put(annotation.value(), annotation.ifFalse()); }
    }

    private void processReplaceOnMethod(Method method, Object obj, Map<String, String> replacements)
            throws InvocationTargetException {
        try {
            Replaces annonation = method.getAnnotation(Replaces.class);
            if (annonation != null) {
                method.invoke(obj, null);
                processReplacesAnnotation(annonation, method.invoke(obj, null), replacements);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void processReplacesAnnotation(@NonNull Replaces annotation, Object obj, Map<String, String> replacements) {
        String path = annotation.value();
        if (annotation.type().equals(Type.STRING)) {
            Object o = obj;
            if (o == null) {
                replacements.put(path, "");
                return;
            }
            Template inner = o.getClass().getDeclaredAnnotation(Template.class);
            if (inner != null) { //Hat ein eigenes Template
                replacements.put(path, parser.parse(o));
            }
            else { //Hat kein eigenes Template
                replacements.put(path, o.toString());
            }
        }
        else if (annotation.type().equals(Type.OBJECT)) {
            addAllWithPrefix(replacements, readReplacements(obj), path);

        }
    }


    private void addAllWithPrefix(Map<String, String> main, Map<String, String> sub, String prefix) {
        sub.forEach((k, v) -> main.put(addDotToPath(prefix) + k, v));
    }


    private String addDotToPath(String pathIdentifier) {
        if (pathIdentifier.length() > 0 && !pathIdentifier.endsWith(".")) pathIdentifier += ".";
        return pathIdentifier;
    }
}
