package com.minidoodle.minidoodle.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SlotRepository
    extends JpaRepository<SlotEntity, Long>, JpaSpecificationExecutor<SlotEntity> {

  Optional<SlotEntity> findByIdAndUserId(Long id, Long userId);

  void deleteByIdAndUserId(Long id, Long userId);
}
