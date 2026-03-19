package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveToMyFilesRequest(
        @NotBlank String folderName,
        @NotBlank String fileName
) {}
