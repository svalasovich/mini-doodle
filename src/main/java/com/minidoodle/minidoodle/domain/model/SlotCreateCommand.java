package com.minidoodle.minidoodle.domain.model;

import java.util.List;

public record SlotCreateCommand(Long userId, List<SlotRange> slots) {}
