package com.minidoodle.minidoodle.domain.service;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotCreateCommand;
import com.minidoodle.minidoodle.domain.model.SlotListQuery;
import com.minidoodle.minidoodle.domain.model.SlotUpdateCommand;
import com.minidoodle.minidoodle.port.in.SlotCreateUseCase;
import com.minidoodle.minidoodle.port.in.SlotDeleteUseCase;
import com.minidoodle.minidoodle.port.in.SlotGetUseCase;
import com.minidoodle.minidoodle.port.in.SlotListUseCase;
import com.minidoodle.minidoodle.port.in.SlotUpdateUseCase;
import com.minidoodle.minidoodle.port.out.SlotCreatePort;
import com.minidoodle.minidoodle.port.out.SlotDeletePort;
import com.minidoodle.minidoodle.port.out.SlotGetPort;
import com.minidoodle.minidoodle.port.out.SlotListPort;
import com.minidoodle.minidoodle.port.out.SlotUpdatePort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlotService
    implements SlotCreateUseCase,
        SlotUpdateUseCase,
        SlotDeleteUseCase,
        SlotListUseCase,
        SlotGetUseCase {

  private final SlotCreatePort slotCreatePort;
  private final SlotUpdatePort slotUpdatePort;
  private final SlotDeletePort slotDeletePort;
  private final SlotListPort slotListPort;
  private final SlotGetPort slotGetPort;

  @Override
  public List<Slot> create(SlotCreateCommand command) {
    var slots =
        command.slots().stream()
            .map(
                range ->
                    new Slot(null, command.userId(), null, range.start(), range.end(), null, null))
            .toList();
    return slotCreatePort.createAll(slots);
  }

  @Override
  public Slot update(SlotUpdateCommand command) {
    var slot =
        new Slot(
            command.slotId(),
            command.userId(),
            null,
            command.range().start(),
            command.range().end(),
            null,
            null);
    slotUpdatePort.update(slot);
    return slot;
  }

  @Override
  public void delete(Long userId, Long slotId) {
    slotDeletePort.delete(userId, slotId);
  }

  @Override
  public Page<Slot> list(SlotListQuery query, Pageable pageable) {
    return slotListPort.list(query, pageable);
  }

  @Override
  public Optional<Slot> get(Long userId, Long slotId) {
    return slotGetPort.get(userId, slotId);
  }
}
