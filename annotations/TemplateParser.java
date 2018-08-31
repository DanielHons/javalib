package de.danielhons.lib.annotations;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {

    public TemplateParser() {
    }

    public TemplateParser(String leftMarker, String rightMarker) {
        this.leftMarker = leftMarker;
        this.rightMarker = rightMarker;
    }

    private String leftMarker="@@";
    private String rightMarker="@@";

    public String parse(Object o){
        Template t =o.getClass().getDeclaredAnnotation(Template.class);
        if (t==null) throw new IllegalArgumentException("Parsing failed: not templated");
        Map<String,String> replacements = new StringReplacementReader().readReplacements(o);
        return replaceInTemplate(replacements,t.value());
    }


    private String replaceInTemplate(Map<String,String> replacements, String template){
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
