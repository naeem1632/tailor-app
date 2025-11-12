package com.example.tailorapp.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes ra) {
        ra.addFlashAttribute("error", AppConstants.MAX_UPLOAD_SIZE_ERROR_MESSAGE);
        return "redirect:/clients";
    }
}
