package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UniFindRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val postDao = database.postDao()
    private val reportDao = database.reportDao()
    private val notificationDao = database.notificationDao()

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("unifind_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        // Load persistent login session
        val userId = sharedPrefs.getString("current_uid", null)
        ioScope.launch {
            if (userId != null) {
                val user = userDao.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    sharedPrefs.edit().remove("current_uid").apply()
                }
            }
            prepopulateDatabaseIfNeeded()
        }
    }

    suspend fun register(
        name: String,
        studentId: String,
        department: String,
        email: String,
        phone: String,
        role: String = "user"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val uid = UUID.randomUUID().toString()
            // Automatic admin role assignment for specific campus administration emails
            val assignedRole = if (email.lowercase().startsWith("admin") || email.lowercase() == "dean@campus.edu") "admin" else role
            val newUser = UserEntity(
                uid = uid,
                name = name,
                studentId = studentId,
                department = department,
                email = email,
                phone = phone,
                photoUrl = "https://api.dicebear.com/7.x/pixel-art/svg?seed=$name",
                role = assignedRole,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser)
            _currentUser.value = newUser
            sharedPrefs.edit().putString("current_uid", uid).apply()
            
            // Send welcome notification
            addNotification(
                title = "Welcome to UniFind, $name!",
                body = "You can now start reporting lost or found items around the university campus."
            )
            
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            // Find user in local database by email
            val allUsers = database.openHelper.writableDatabase // Force open
            // Query local users first
            var targetUser: UserEntity? = userDao.getUserByEmail(email)
            
            if (targetUser == null) {
                // For direct developer experience, if user is mock logging in, create one to avoid empty state block!
                val cleanName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                val fallbackUid = "demo_uid_${UUID.randomUUID().toString().take(6)}"
                val finalRole = if (email.lowercase().startsWith("admin")) "admin" else "user"
                val demoUser = UserEntity(
                    uid = fallbackUid,
                    name = cleanName,
                    studentId = "STU-${(1000..9999).random()}",
                    department = "Computer Science",
                    email = email,
                    phone = "+1 (555) 019-2831",
                    photoUrl = "https://api.dicebear.com/7.x/pixel-art/svg?seed=$cleanName",
                    role = finalRole,
                    createdAt = System.currentTimeMillis()
                )
                userDao.insertUser(demoUser)
                targetUser = demoUser
            }

            val loggedInUser = targetUser!!
            _currentUser.value = loggedInUser
            sharedPrefs.edit().putString("current_uid", loggedInUser.uid).apply()
            
            addNotification(
                title = "Logged in successfully",
                body = "Active session restored for ${loggedInUser.name} (${loggedInUser.role})."
            )
            
            Result.success(loggedInUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        name: String,
        studentId: String,
        department: String,
        phone: String,
        photoUrl: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val updatedUser = user.copy(
                name = name,
                studentId = studentId,
                department = department,
                phone = phone,
                photoUrl = photoUrl
            )
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        _currentUser.value = null
        sharedPrefs.edit().remove("current_uid").apply()
    }

    // Posts API
    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPostsFlow()
    fun getActivePosts(): Flow<List<PostEntity>> = postDao.getActivePostsFlow()
    fun getMyPosts(uid: String): Flow<List<PostEntity>> = postDao.getPostsByUserIdFlow(uid)

    suspend fun getPostById(postId: String): PostEntity? = withContext(Dispatchers.IO) {
        postDao.getPostById(postId)
    }

    suspend fun createPost(
        type: String,
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        location: String,
        gpsLocation: String,
        date: String,
        contactInfo: String
    ): Result<PostEntity> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("User must be logged in to post"))
        try {
            val postId = UUID.randomUUID().toString()
            val newPost = PostEntity(
                postId = postId,
                uid = user.uid,
                type = type.lowercase(),
                title = title,
                description = description,
                category = category,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1553095083-d92ed6ee8e74?auto=format&fit=crop&w=600&q=80" },
                location = location,
                gpsLocation = gpsLocation,
                date = date,
                contactInfo = contactInfo.ifBlank { "${user.email} / ${user.phone}" },
                status = "active",
                createdAt = System.currentTimeMillis()
            )
            postDao.insertPost(newPost)
            
            // Trigger campus alert matching post keyword!
            triggerPostAlert(newPost)
            
            Result.success(newPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(post: PostEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            postDao.updatePost(post)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            postDao.deletePost(postId)
            reportDao.deleteReportsForPost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markPostAsResolved(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val post = postDao.getPostById(postId)
            if (post != null) {
                val updated = post.copy(status = "resolved")
                postDao.updatePost(updated)
                
                // Add notifications alert
                addNotification(
                    title = "Item marked as Resolved",
                    body = "The item \"${post.title}\" has been successfully marked as resolved."
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reports API
    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReportsFlow()

    suspend fun reportPost(postId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Must be logged in to report"))
        try {
            val reportId = UUID.randomUUID().toString()
            val report = ReportEntity(
                reportId = reportId,
                postId = postId,
                reportedBy = user.uid,
                reason = reason,
                createdAt = System.currentTimeMillis()
            )
            reportDao.insertReport(report)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dismissReport(reportId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            reportDao.deleteReport(reportId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun banUser(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Hard delete user from listings or mark role as banned
            val user = userDao.getUserById(uid)
            if (user != null) {
                // Update to role empty or delete post
                userDao.deleteUser(uid)
                addNotification(
                    title = "Campus Security Update",
                    body = "Account for student `${user.name}` has been deactivated for violating policies."
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Notifications API
    fun getNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }

    suspend fun addNotification(title: String, body: String) = withContext(Dispatchers.IO) {
        val notification = NotificationEntity(
            notificationId = UUID.randomUUID().toString(),
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)
    }

    private suspend fun triggerPostAlert(post: PostEntity) {
        val titleText = if (post.type == "lost") "🚨 LOST ITEM REPORTED" else "📢 FOUND ITEM REPORTED"
        val bodyText = if (post.type == "lost") {
            "Please watch out for a \"${post.title}\" reported lost at ${post.location}."
        } else {
            "A \"${post.title}\" was successfully recovered at ${post.location}. Is it yours?"
        }
        addNotification(titleText, bodyText)
    }

    private suspend fun prepopulateDatabaseIfNeeded() {
        // Double check open database connection
        var existingPosts: List<PostEntity> = emptyList()
        try {
            existingPosts = postDao.getAllPostsFlow().first()
        } catch (e: Exception) {
            // Safe fallback
        }
        
        if (existingPosts.isEmpty()) {
            val adminUser = UserEntity(
                uid = "admin_faculty_uid",
                name = "Office of Campus Support",
                studentId = "FAC-9121",
                department = "Administration",
                email = "admin@unifind.edu",
                phone = "+1 (555) 911-0001",
                photoUrl = "https://api.dicebear.com/7.x/pixel-art/svg?seed=Office",
                role = "admin",
                createdAt = System.currentTimeMillis()
            )
            val regularUser = UserEntity(
                uid = "student_alex_uid",
                name = "Alex Rivera",
                studentId = "STU-8821",
                department = "Engineering",
                email = "alex@student.edu",
                phone = "+1 (555) 012-9922",
                photoUrl = "https://api.dicebear.com/7.x/pixel-art/svg?seed=Alex",
                role = "user",
                createdAt = System.currentTimeMillis()
            )

            userDao.insertUser(adminUser)
            userDao.insertUser(regularUser)

            val samples = listOf(
                PostEntity(
                    postId = "post_sample_1",
                    uid = "student_alex_uid",
                    type = "lost",
                    title = "MacBook Pro 14\" M2 Space Gray",
                    description = "Left my MacBook Pro on the third floor of the Library in the study kiosk. It has a custom dark blue sticker with stickers 'Kotlin' and 'Campus Hack' on the top corner. Please return it, contains my final project thesis!",
                    category = "Electronics",
                    imageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80",
                    location = "Main Library Floor 3, Kiosk 12",
                    gpsLocation = "40.7128,-74.0060",
                    date = "2026-05-22",
                    contactInfo = "alex@student.edu / +1 (555) 012-9922",
                    status = "active",
                    createdAt = System.currentTimeMillis() - 12000000 // 3 hours ago
                ),
                PostEntity(
                    postId = "post_sample_2",
                    uid = "admin_faculty_uid",
                    type = "found",
                    title = "Apple Airpods Pro 2 Case",
                    description = "Found a white AirPods Pro case with zero stickers in front of the Campus Gym auditorium stairs. The name displayed when connected suggests 'Lucas' AirPods. Turned into safety desk.",
                    category = "Electronics",
                    imageUrl = "https://images.unsplash.com/photo-1608156639585-b3a032ef9689?auto=format&fit=crop&w=600&q=80",
                    location = "Campus Gym Lobby",
                    gpsLocation = "40.7132,-74.0048",
                    date = "2026-05-21",
                    contactInfo = "Safety Office, Admin Hall Room 102",
                    status = "active",
                    createdAt = System.currentTimeMillis() - 25000000 // 7 hours ago
                ),
                PostEntity(
                    postId = "post_sample_3",
                    uid = "student_alex_uid",
                    type = "found",
                    title = "Brown Leather Fossil Wallet",
                    description = "Found a neat brown leather wallet lying under the bench at the Central Campus Bus stop. Contains student laundry card and some loose notes but no major ID. Reach out if yours!",
                    category = "Wallet",
                    imageUrl = "https://images.unsplash.com/photo-1627124765135-56c33fc36eab?auto=format&fit=crop&w=600&q=80",
                    location = "Central Bus Stop",
                    gpsLocation = "40.7140,-74.0090",
                    date = "2026-05-23",
                    contactInfo = "alex@student.edu",
                    status = "active",
                    createdAt = System.currentTimeMillis() - 5000000 // Just over an hour ago
                ),
                PostEntity(
                    postId = "post_sample_4",
                    uid = "admin_faculty_uid",
                    type = "lost",
                    title = "Keychain with Corvette Ring & USB",
                    description = "Lost a ring of keys with a Corvette metal key fob and an orange 32GB SanDisk USB key. Likely dropped during the morning walking path from Faculty Parking to Science Hall B.",
                    category = "Keys",
                    imageUrl = "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=600&q=80",
                    location = "Science Hall West Path",
                    gpsLocation = "40.7111,-74.0081",
                    date = "2026-05-23",
                    contactInfo = "admin@unifind.edu / Faculty Admin Front Desk",
                    status = "resolved", // Resolved!
                    createdAt = System.currentTimeMillis() - 86000000 // 24 hours ago
                )
            )

            for (sample in samples) {
                postDao.insertPost(sample)
            }

            addNotification(
                title = "Campus Operations Initialized",
                body = "Welcome to UniFind. Real-time campus Lost & Found database is active."
            )
        }
    }
}
