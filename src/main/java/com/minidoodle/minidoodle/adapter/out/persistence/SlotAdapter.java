package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.Slot;
import com.minidoodle.minidoodle.domain.model.SlotListQuery;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlotAdapter
    implements SlotCreatePort, SlotUpdatePort, SlotDeletePort, SlotListPort, SlotGetPort {

  private final SlotRepository slotRepository;
  private final SlotEntityMapper slotEntityMapper;

  @Override
  public List<Slot> createAll(List<Slot> slots) {
    var entities = slots.stream().map(slotEntityMapper::toEntity).toList();
    var saved = slotRepository.saveAll(entities);
    return saved.stream().map(slotEntityMapper::toModel).toList();
  }

  @Override
  public void update(Slot slot) {
    slotRepository
        .findByIdAndUserId(slot.id(), slot.userId())
        .ifPresent(
            entity -> {
              entity.setStartTime(slot.startTime());
              entity.setEndTime(slot.endTime());
              slotRepository.save(entity);
            });
  }

  @Override
  public void delete(Long userId, Long slotId) {
    slotRepository.deleteByIdAndUserId(slotId, userId);
  }

  @Override
  public Page<Slot> list(SlotListQuery query, Pageable pageable) {
    Specification<SlotEntity> spec = (root, cq, cb) -> cb.equal(root.get("userId"), query.userId());
    if (query.from() != null) {
      spec =
          spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), query.from()));
    }
    if (query.to() != null) {
      spec = spec.and((root, cq, cb) -> cb.lessThan(root.get("startTime"), query.to()));
    }
    if (query.available() != null) {
      spec =
          query.available()
              ? spec.and((root, cq, cb) -> cb.isNull(root.get("meetingId")))
              : spec.and((root, cq, cb) -> cb.isNotNull(root.get("meetingId")));
    }
    return slotRepository.findAll(spec, pageable).map(slotEntityMapper::toModel);
  }

  @Override
  public Optional<Slot> get(Long userId, Long slotId) {
    return slotRepository.findByIdAndUserId(slotId, userId).map(slotEntityMapper::toModel);
  }
}
