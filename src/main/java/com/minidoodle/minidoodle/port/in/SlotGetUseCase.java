package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.Slot;
import java.util.Optional;

public interface SlotGetUseCase {
  Optional<Slot> get(Long userId, Long slotId);
}
