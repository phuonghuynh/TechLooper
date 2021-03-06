package com.techlooper.dto;

import java.io.Serializable;

/**
 * Created by phuonghqh on 8/26/15.
 */
public class JoinBySocialDto implements Serializable {

  private Long id;

  private String firstName;

  private String lastName;

  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}
