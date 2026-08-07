package com.minidoodle.minidoodle.adapter.in.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 128) String name,
        @Email String email
) {
}