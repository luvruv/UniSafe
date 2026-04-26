package com.example.unisafe.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String studentId;
    private String block;
    private String roomNumber;
    private String gender;
    private String dateOfBirth;
    private String role; // "student" or "admin"
    private String profileImageUrl;

    public User() {}

    public User(String id, String name, String email, String phone,
                String studentId, String block, String roomNumber,
                String gender, String dateOfBirth, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.studentId = studentId;
        this.block = block;
        this.roomNumber = roomNumber;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.profileImageUrl = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}