package com.campusexchange.config;

import java.util.Locale;
import java.util.regex.Pattern;

/** Exact college domain; this restriction does not verify mailbox ownership. */
public final class CollegeEmailPolicy {
  private static final Pattern COLLEGE_EMAIL = Pattern.compile("^[^\\s@]+@thapar\\.edu$");

  private CollegeEmailPolicy() {}

  public static String normalize(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }

  public static boolean isAllowed(String email) {
    return COLLEGE_EMAIL.matcher(normalize(email)).matches();
  }
}
