package com.minidoodle.minidoodle.port.out;

import com.minidoodle.minidoodle.domain.model.Slot;
import java.util.Optional;

public interface SlotGetPort {
  Optional<Slot> get(Long userId, Long slotId);
}
