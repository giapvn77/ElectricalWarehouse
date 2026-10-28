package com.electricalwarehouse.integration;

import java.util.List;

/** Format-neutral table handed to the Excel/PDF exporters. */
public record TableData(String title, List<String> subtitleLines, List<String> headers, List<List<Object>> rows) {
}
