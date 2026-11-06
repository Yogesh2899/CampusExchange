package com.campusexchange.config;

import com.campusexchange.model.User;
import com.campusexchange.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rechecks saved accounts so suspension also revokes existing browser sessions. */
public class AccountStatusFilter extends OncePerRequestFilter {
  private final UserRepository users;

  public AccountStatusFilter(UserRepository users) {
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof User user) {
      var fresh = users.findById(user.getId());
      if (fresh.isPresent() && !CollegeEmailPolicy.isAllowed(fresh.get().getEmail())) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        response.sendRedirect(request.getContextPath() + "/login?college");
        return;
      }
      if (fresh.isEmpty() || !fresh.get().isEnabled()) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        response.sendRedirect(request.getContextPath() + "/login?suspended");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
