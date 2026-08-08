package com.minidoodle.minidoodle.domain.model;

public record SlotUpdateCommand(Long userId, Long slotId, SlotRange range) {}
