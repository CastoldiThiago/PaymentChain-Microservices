package com.paymentchain.customer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class SortParamSanitizingFilter extends OncePerRequestFilter {

    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
            "id", "customerId", "name", "surname", "dni", "email"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean needsSanitize = false;
        Map<String, String[]> params = request.getParameterMap();
        for (String name : params.keySet()) {
            if (isSortParameterName(name)) {
                String[] values = params.get(name);
                if (values != null) {
                    for (String v : values) if (v != null && looksLikeArray(v)) { needsSanitize = true; break; }
                }
            }
            if (needsSanitize) break;
        }
        if (needsSanitize) {
            HttpServletRequest wrapped = new SanitizingRequestWrapper(request);
            filterChain.doFilter(wrapped, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean looksLikeArray(String s) {
        s = s.trim();
        return s.startsWith("[") && s.endsWith("]");
    }

    // Class-level helper so outer methods can call it
    private static boolean isSortParameterName(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase(Locale.ROOT);
        return n.equals("sort") || n.equals("sort[]") || n.startsWith("sort[") || n.startsWith("sort%5b");
    }

    private static class SanitizingRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> modifiedParams;

        public SanitizingRequestWrapper(HttpServletRequest request) {
            super(request);
            Map<String, String[]> original = request.getParameterMap();
            modifiedParams = new HashMap<>(original);
            // sanitize all parameters that are sort-related (sort, sort[])
            for (String name : original.keySet()) {
                if (isSortParameterName(name)) {
                    String[] sortValues = original.get(name);
                    if (sortValues != null && sortValues.length > 0) {
                        String[] sanitized = Arrays.stream(sortValues)
                                .map(SanitizingRequestWrapper::sanitizeSingleSortValue)
                                .filter(v -> v != null && !v.isBlank())
                                .map(v -> {
                                    String field = v.split(",")[0];
                                    return ALLOWED_SORT_FIELDS.contains(field) ? v : null;
                                })
                                .filter(v -> v != null && !v.isBlank())
                                .toArray(String[]::new);
                        if (sanitized.length > 0) {
                            modifiedParams.put(name, sanitized);
                        } else {
                            modifiedParams.remove(name);
                        }
                    }
                }
            }
        }

        @Override
        public String getParameter(String name) {
            String[] vals = modifiedParams.get(name);
            if (vals == null || vals.length == 0) return null;
            return vals[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return modifiedParams.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(modifiedParams);
        }

        private static String sanitizeSingleSortValue(String raw) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1);
                s = s.replaceAll("\"", "");
                s = s.trim();
                s = s.replaceAll("\\s+", "");
                return s;
            }
            return raw;
        }
        private static boolean isSortParameterName(String name) {
            if (name == null) return false;
            String n = name.trim().toLowerCase(Locale.ROOT);
            return n.equals("sort") || n.equals("sort[]") || n.startsWith("sort[") || n.startsWith("sort%5b");
        }
    }
}
