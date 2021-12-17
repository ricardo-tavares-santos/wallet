package com.ricardo.demo.model;

import javax.persistence.*;

@Entity
@Table(name = "player")
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "password")
	private String password;

    @Column(name = "admin")
	private boolean admin;

	public Player() {
	}

	public Player(String name, boolean admin, String email, String password) {
		this.name = name;
		this.admin = admin;
		this.email = email;
		this.password = password;
	}

	public long getId() {
		return id;
	}	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean getAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString() {
		return "Player [id=" + id + ", name=" + name + ", admin=" + admin + ", email=" + email + ", password=" + password + "]";
	}
}
