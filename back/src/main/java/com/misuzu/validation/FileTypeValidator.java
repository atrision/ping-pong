package com.misuzu.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {
    private String[] allowed;

    @Override
    public void initialize(FileType constraintAnnotation) {
        this.allowed = constraintAnnotation.allowed();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) return false;
        String contentType = file.getContentType();
        return contentType != null && Arrays.asList(allowed).contains(contentType);
    }
}