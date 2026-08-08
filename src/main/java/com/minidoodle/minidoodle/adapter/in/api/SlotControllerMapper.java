package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotCreateCommand;
import com.minidoodle.minidoodle.domain.model.SlotListQuery;
import com.minidoodle.minidoodle.domain.model.SlotRange;
import com.minidoodle.minidoodle.domain.model.SlotUpdateCommand;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class SlotControllerMapper {

  public SlotCreateCommand toCreateCommand(
      @NotNull Long userId, @NotNull List<SlotRangeRequest> requests) {
    var ranges = requests.stream().map(r -> new SlotRange(r.start(), r.end())).toList();
    return new SlotCreateCommand(userId, ranges);
  }

  public SlotUpdateCommand toUpdateCommand(
      @NotNull Long userId, @NotNull Long slotId, @NotNull SlotRangeRequest request) {
    return new SlotUpdateCommand(userId, slotId, new SlotRange(request.start(), request.end()));
  }

  public SlotListQuery toListQuery(
      @NotNull Long userId, Instant from, Instant to, Boolean available) {
    return new SlotListQuery(userId, from, to, available);
  }

  public List<SlotResponse> toSlotResponses(@NotNull List<Slot> slots) {
    return slots.stream().map(this::toSlotResponse).toList();
  }

  public SlotResponse toSlotResponse(Slot slot) {
    return new SlotResponse(
        slot.id(), slot.userId(), slot.startTime(), slot.endTime(), slot.isAvailable());
  }
}
