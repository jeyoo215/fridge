package com.example.backend.domain.shopping.dto;

import java.util.List;

public record ReorderItemsRequest(List<Long> itemIds) {}