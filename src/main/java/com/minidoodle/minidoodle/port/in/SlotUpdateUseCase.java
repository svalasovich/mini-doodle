package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotUpdateCommand;

public interface SlotUpdateUseCase {
  Slot update(SlotUpdateCommand command);
}
