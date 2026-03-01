package com.example.check.validator;
import com.example.check.define.FieldRule;
import com.example.check.define.ParamValidate;
import java.lang.reflect.Field;
public class ParamValidator {
    public static boolean validate(ParamValidate validate, Object request) {
        if (validate == null || validate.getFields() == null || request == null) return true;
        try {
            for (FieldRule rule : validate.getFields()) {
                Field f = request.getClass().getDeclaredField(rule.getName());
                f.setAccessible(true);
                Object val = f.get(request);
                if (rule.isRequired() && val == null) return false;
                if (val == null) continue;
                if ("decimal".equals(rule.getType())) {
                    double d = Double.parseDouble(val.toString());
                    if (rule.getMin() != null && d < Double.parseDouble(rule.getMin())) return false;
                }
                if (rule.getLength() != null) {
                    String[] ss = rule.getLength().split(",");
                    int min = Integer.parseInt(ss[0]);
                    int max = Integer.parseInt(ss[1]);
                    String s = val.toString();
                    if (s.length() < min || s.length() > max) return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

