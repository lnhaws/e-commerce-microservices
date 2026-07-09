package com.rainbowforest.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.persistence.*;

@Entity
@Table (name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "user_name", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, message = "Tên đăng nhập phải có ít nhất 4 ký tự")
    private String userName;

    @Column (name = "user_password", nullable = false, length = 255)
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String userPassword;
    
	@Column(name = "active")
    private Integer active;

    @OneToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "user_details_id")
    @Valid
    private UserDetails userDetails;

    @ManyToOne
    @JoinColumn (name = "role_id")
    private UserRole role;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

	public UserDetails getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDetails userDetails) {
		this.userDetails = userDetails;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}
}
