package com.minidoodle.minidoodle.port.out;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotListQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlotListPort {
  Page<Slot> list(SlotListQuery query, Pageable pageable);
}
