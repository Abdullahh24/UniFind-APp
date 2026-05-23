package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val studentId: String,
    val department: String,
    val email: String,
    val phone: String,
    val photoUrl: String,
    val role: String, // "user" or "admin"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val uid: String,
    val type: String, // "lost" or "found"
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val location: String,
    val gpsLocation: String, // "latitude, longitude" format
    val date: String,
    val contactInfo: String,
    val status: String, // "active" or "resolved"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val postId: String,
    val reportedBy: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
