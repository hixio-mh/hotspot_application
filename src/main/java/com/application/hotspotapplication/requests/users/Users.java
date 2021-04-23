package com.application.hotspotapplication.requests.users;

import com.application.hotspotapplication.requests.hotspots.HotspotLocation.Hotspot;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class Users {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "email")
  private String email;

  @Column(name = "active")
  private Boolean active;

  @OneToMany(mappedBy = "users")
  private List<Hotspot> hotspots;
}