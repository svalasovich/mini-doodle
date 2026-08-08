package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotCreateCommand;
import java.util.List;

public interface SlotCreateUseCase {
  List<Slot> create(SlotCreateCommand command);
}
