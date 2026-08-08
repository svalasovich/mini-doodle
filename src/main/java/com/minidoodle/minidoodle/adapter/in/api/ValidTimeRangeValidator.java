package com.minidoodle.minidoodle.adapter.in.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTimeRangeValidator
    implements ConstraintValidator<ValidTimeRange, SlotRangeRequest> {

  @Override
  public boolean isValid(SlotRangeRequest request, ConstraintValidatorContext context) {
    if (request == null || request.start() == null || request.end() == null) {
      return true;
    }
    return request.end().isAfter(request.start());
  }
}
