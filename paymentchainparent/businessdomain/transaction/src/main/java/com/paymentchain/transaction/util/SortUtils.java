package com.paymentchain.transaction.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SortUtils {

    public static Sort parseSortParams(String[] rawSortParams, Set<String> allowedFields, Sort fallback) {
        if (rawSortParams == null || rawSortParams.length == 0) return fallback;
        List<Sort.Order> orders = new ArrayList<>();
        for (String raw : rawSortParams) {
            if (raw == null || raw.isBlank()) continue;
            String sanitized = sanitize(raw);
            if (sanitized.isBlank()) continue;
            // take first segment as field, rest may be direction
            String[] parts = sanitized.split(",");
            String field = parts[0].trim();
            if (!allowedFields.contains(field)) continue; // skip not allowed
            Sort.Direction dir = Sort.Direction.ASC;
            if (parts.length > 1) {
                String d = parts[1].trim().toUpperCase(Locale.ROOT);
                if ("DESC".equals(d) || "DESCENDING".equals(d)) dir = Sort.Direction.DESC;
            }
            orders.add(new Sort.Order(dir, field));
        }
        if (orders.isEmpty()) return fallback;
        return Sort.by(orders);
    }

    private static String sanitize(String raw) {
        String s = raw.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
            s = s.replaceAll("\"", "");
            s = s.replaceAll("\\s+", "");
            return s;
        }
        return s;
    }
}
