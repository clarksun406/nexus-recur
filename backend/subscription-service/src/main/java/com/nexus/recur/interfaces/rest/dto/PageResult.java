package com.nexus.recur.interfaces.rest.dto;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int limit, long total) {
}
