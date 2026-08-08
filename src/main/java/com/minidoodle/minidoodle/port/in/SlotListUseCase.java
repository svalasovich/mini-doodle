package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotListQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlotListUseCase {
  Page<Slot> list(SlotListQuery query, Pageable pageable);
}
