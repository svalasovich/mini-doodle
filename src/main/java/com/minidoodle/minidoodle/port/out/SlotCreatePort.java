package com.minidoodle.minidoodle.port.out;

import com.minidoodle.minidoodle.domain.model.Slot;
import java.util.List;

public interface SlotCreatePort {
  List<Slot> createAll(List<Slot> slots);
}
