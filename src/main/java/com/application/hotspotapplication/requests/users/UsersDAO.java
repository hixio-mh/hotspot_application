package com.application.hotspotapplication.requests.users;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersDAO extends JpaRepository<Users, Long> {

  Users findUsersById(Long id);

  //@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
  Users findUsersByEmail(String email);

}