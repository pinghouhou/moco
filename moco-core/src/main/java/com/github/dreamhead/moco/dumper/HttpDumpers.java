package com.github.dreamhead.moco.dumper;

import com.github.dreamhead.moco.HttpMessage;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HttpDumpers {
    public static String asContent(final HttpMessage message) {
        long length = getContentLength(message, -1);
        if (length > 0) {
            return StringUtil.NEWLINE + StringUtil.NEWLINE + contentForDump(message);
        }

        return "";
    }

    private static String contentForDump(final HttpMessage message) {
        String type = message.getHeader(HttpHeaders.CONTENT_TYPE);
        if (isText(type)) {
            return message.getContent().toString();
        }

        return "<content is binary>";
    }

    private static boolean isText(final String type) {
        try {
            MediaType mediaType = MediaType.parse(type);
            return mediaType.is(MediaType.ANY_TEXT_TYPE)
                    || mediaType.subtype().endsWith("javascript")
                    || mediaType.subtype().endsWith("json")
                    || mediaType.subtype().endsWith("xml")
                    || mediaType.is(MediaType.FORM_DATA)
                    || mediaType.subtype().endsWith("form-data");
        } catch (Exception e) {
            return false;
        }
    }

    private static long getContentLength(final HttpMessage response, final long defaultValue) {
        String lengthText = response.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (lengthText != null) {
            try {
                return Long.parseLong(lengthText);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private final static Joiner.MapJoiner headerJoiner = Joiner.on(StringUtil.NEWLINE).withKeyValueSeparator(": ");

    public static String asHeaders(final HttpMessage message) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : message.getHeaders().entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                entries.add(Maps.immutableEntry(key, value));
            }
        }

        return headerJoiner.join(entries);
    }

    private HttpDumpers() {
    }
}
