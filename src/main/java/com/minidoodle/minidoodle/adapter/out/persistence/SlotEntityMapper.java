package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.Slot;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class SlotEntityMapper {

  public SlotEntity toEntity(@NotNull Slot slot) {
    return new SlotEntity(
        slot.id(),
        slot.userId(),
        slot.meetingId(),
        slot.startTime(),
        slot.endTime(),
        slot.createdAt(),
        slot.updatedAt());
  }

  public Slot toModel(@NotNull SlotEntity entity) {
    return new Slot(
        entity.getId(),
        entity.getUserId(),
        entity.getMeetingId(),
        entity.getStartTime(),
        entity.getEndTime(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
