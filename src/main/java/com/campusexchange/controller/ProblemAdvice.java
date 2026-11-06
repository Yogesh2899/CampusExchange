package com.campusexchange.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ProblemAdvice {
  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HandlerMethodValidationException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String badInput(Exception ex, Model model) {
    model.addAttribute("title", "Please check your details");
    model.addAttribute(
        "message", "Choose valid options, complete required fields, and use numbers for prices.");
    return "problem";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String invalid(IllegalArgumentException ex, Model model) {
    model.addAttribute("title", "Please check your details");
    model.addAttribute("message", ex.getMessage());
    return "problem";
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public String conflict(Model model) {
    model.addAttribute("title", "That information has changed");
    model.addAttribute(
        "message",
        "The action could not be completed because a record already exists or is in use. Refresh"
            + " the page and try again.");
    return "problem";
  }
}
