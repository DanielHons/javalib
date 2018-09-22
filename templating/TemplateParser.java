package de.danielhons.lib.templating;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {

    private String leftMarker = "@@";
    private String rightMarker = "@@";

    public TemplateParser() {
    }
    public TemplateParser(String leftMarker, String rightMarker) {
        this.leftMarker = leftMarker;
        this.rightMarker = rightMarker;
    }

    public String parse(Object o) {
        Template t = o.getClass().getDeclaredAnnotation(Template.class);

        String templatePlainText = getTemplateSource(t);
        if (t == null) throw new IllegalArgumentException("Parsing failed: class not marked as template");
        Map<String, String> replacements = new StringReplacementReader().readReplacements(o);
        return replaceInTemplate(replacements, templatePlainText);
    }

    private String getTemplateSource(Template t) {
        switch (t.source()) {
            case VALUE:
                return t.value();
            case FILE:
                InputStream is = getClass().getClassLoader().getResourceAsStream(t.value());
                byte[] encoded = new byte[0];
                try {
                    encoded = IOUtils.toByteArray(is);
                    return new String(encoded, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        return "";
    }


    public String replaceInTemplate(Map<String, String> replacements, String template) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String regex = Pattern.quote(transformKey(entry.getKey()));
            template = template.replaceAll(regex, Matcher.quoteReplacement(entry.getValue()));
        }
        return template;
    }

    private String transformKey(String plainKey) {
        return leftMarker + plainKey + rightMarker;
    }
}
