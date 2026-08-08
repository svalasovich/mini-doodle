package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.port.in.SlotCreateUseCase;
import com.minidoodle.minidoodle.port.in.SlotDeleteUseCase;
import com.minidoodle.minidoodle.port.in.SlotGetUseCase;
import com.minidoodle.minidoodle.port.in.SlotListUseCase;
import com.minidoodle.minidoodle.port.in.SlotUpdateUseCase;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/users/{userId}/slots")
@RequiredArgsConstructor
public class SlotController {

  private final SlotCreateUseCase slotCreateUseCase;
  private final SlotUpdateUseCase slotUpdateUseCase;
  private final SlotDeleteUseCase slotDeleteUseCase;
  private final SlotListUseCase slotListUseCase;
  private final SlotGetUseCase slotGetUseCase;
  private final SlotControllerMapper slotControllerMapper;

  @GetMapping("/{slotId}")
  public ResponseEntity<SlotResponse> get(@PathVariable Long userId, @PathVariable Long slotId) {
    var response = slotGetUseCase.get(userId, slotId).map(slotControllerMapper::toSlotResponse);
    return ResponseEntity.of(response);
  }

  @PostMapping
  public ResponseEntity<List<SlotResponse>> create(
      @PathVariable Long userId, @RequestBody List<@Valid SlotRangeRequest> requests) {
    var command = slotControllerMapper.toCreateCommand(userId, requests);
    var slots = slotCreateUseCase.create(command);
    return ResponseEntity.ok(slotControllerMapper.toSlotResponses(slots));
  }

  @PutMapping("/{slotId}")
  public ResponseEntity<SlotResponse> update(
      @PathVariable Long userId,
      @PathVariable Long slotId,
      @RequestBody @Valid SlotRangeRequest request) {
    var command = slotControllerMapper.toUpdateCommand(userId, slotId, request);
    var slot = slotUpdateUseCase.update(command);
    return ResponseEntity.ok(slotControllerMapper.toSlotResponse(slot));
  }

  @DeleteMapping("/{slotId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long slotId) {
    slotDeleteUseCase.delete(userId, slotId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<Page<SlotResponse>> list(
      @PathVariable Long userId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Boolean available,
      Pageable pageable) {
    var query = slotControllerMapper.toListQuery(userId, from, to, available);
    var page = slotListUseCase.list(query, pageable).map(slotControllerMapper::toSlotResponse);
    return ResponseEntity.ok(page);
  }
}
