package com.example.unisafe.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

/**
 * Represents a single campus maintenance complaint.
 *
 * Field naming matches Firestore document keys exactly so that
 * {@code doc.toObject(Complaint.class)} works without custom deserializers.
 *
 * ─── Firestore Schema ───────────────────────────────────────
 *  id          : String   – document ID (matches Firestore doc ID)
 *  category    : String   – AppConstants.CATEGORY_*
 *  subject     : String   – short title
 *  description : String   – full detail
 *  status      : String   – AppConstants.STATUS_*
 *  priority    : String   – AppConstants.PRIORITY_*
 *  imageUri    : String   – Firebase Storage download URL (or "")
 *  timestamp   : String   – human-readable display string (legacy)
 *  createdAt   : Timestamp – server timestamp for reliable ordering (new)
 *  userId      : String
 *  userName    : String
 *  block       : String
 *  roomNumber  : String
 *  adminNote   : String
 *  assignedTo  : String
 * ────────────────────────────────────────────────────────────
 */
public class Complaint {

    private String    id;
    private String    category;
    private String    subject;
    private String    description;
    private String    status;       // pending | in_progress | completed | rejected
    private String    priority;     // low | medium | high
    private String    imageUri;
    private String    timestamp;    // Display string – kept for backward compat
    private Timestamp createdAt;    // ✅ Server timestamp – use for orderBy queries
    private String    userId;
    private String    userName;
    private String    block;
    private String    roomNumber;
    private String    adminNote;
    private String    assignedTo;

    /** Required no-arg constructor for Firestore deserialization. */
    public Complaint() {}

    // ─── Getters ──────────────────────────────────────────────────────────────

    @Nullable public String    getId()          { return id; }
    @Nullable public String    getCategory()    { return category; }
    @Nullable public String    getSubject()     { return subject; }
    @Nullable public String    getDescription() { return description; }
    @Nullable public String    getStatus()      { return status; }
    @Nullable public String    getPriority()    { return priority; }
    @Nullable public String    getImageUri()    { return imageUri; }
    @Nullable public String    getTimestamp()   { return timestamp; }
    @Nullable public Timestamp getCreatedAt()  { return createdAt; }
    @Nullable public String    getUserId()      { return userId; }
    @Nullable public String    getUserName()    { return userName; }
    @Nullable public String    getBlock()       { return block; }
    @Nullable public String    getRoomNumber()  { return roomNumber; }
    @Nullable public String    getAdminNote()   { return adminNote; }
    @Nullable public String    getAssignedTo()  { return assignedTo; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(@NonNull String id)               { this.id = id; }
    public void setCategory(String category)            { this.category = category; }
    public void setSubject(String subject)              { this.subject = subject; }
    public void setDescription(String description)      { this.description = description; }
    public void setStatus(String status)                { this.status = status; }
    public void setPriority(String priority)            { this.priority = priority; }
    public void setImageUri(String imageUri)            { this.imageUri = imageUri; }
    public void setTimestamp(String timestamp)          { this.timestamp = timestamp; }
    public void setCreatedAt(Timestamp createdAt)       { this.createdAt = createdAt; }
    public void setUserId(String userId)                { this.userId = userId; }
    public void setUserName(String userName)            { this.userName = userName; }
    public void setBlock(String block)                  { this.block = block; }
    public void setRoomNumber(String roomNumber)        { this.roomNumber = roomNumber; }
    public void setAdminNote(String adminNote)          { this.adminNote = adminNote; }
    public void setAssignedTo(String assignedTo)        { this.assignedTo = assignedTo; }
}