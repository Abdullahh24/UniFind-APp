package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.UniFindRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UniFindViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UniFindRepository(application)
    private val sharedPrefs = application.getSharedPreferences("unifind_settings", Context.MODE_PRIVATE)

    // Auth State
    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    // Dark Mode preference
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Selected post ID for details screen
    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    // Dashboard filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow("all") // "all", "lost", "found"
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow("all") // "all", "electronics", "wallet", "keys", "book", "documents", "other"
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    // Master lists from Room
    val allPosts: StateFlow<List<PostEntity>> = repository.getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin lists
    val allReports: StateFlow<List<ReportEntity>> = repository.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Posts for display (Dynamic local search and filters)
    val filteredPosts: StateFlow<List<PostEntity>> = combine(
        allPosts,
        _searchQuery,
        _typeFilter,
        _categoryFilter
    ) { posts, query, type, category ->
        posts.filter { post ->
            val matchesQuery = query.isBlank() || 
                    post.title.contains(query, ignoreCase = true) || 
                    post.description.contains(query, ignoreCase = true) ||
                    post.location.contains(query, ignoreCase = true)

            val matchesType = type == "all" || post.type == type.lowercase()
            
            val matchesCategory = category == "all" || post.category.lowercase() == category.lowercase()

            matchesQuery && matchesType && matchesCategory
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Users list for admin
    private val _usersList = MutableStateFlow<List<UserEntity>>(emptyList())
    val usersList: StateFlow<List<UserEntity>> = _usersList.asStateFlow()

    private val database = com.example.data.database.AppDatabase.getDatabase(application)
    
    init {
        // Collect users real-time for admin panel
        viewModelScope.launch {
            database.userDao().getAllUsersFlow().collect {
                _usersList.value = it
            }
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        val nextVal = !_isDarkMode.value
        _isDarkMode.value = nextVal
        sharedPrefs.edit().putBoolean("dark_mode", nextVal).apply()
        
        viewModelScope.launch {
            repository.addNotification(
                title = "Theme Changed",
                body = "System theme updated to ${if (nextVal) "Dark Mode" else "Light Mode"}."
            )
        }
    }

    // Screen navigation
    fun navigateTo(screen: String, postId: String? = null) {
        if (postId != null) {
            _selectedPostId.value = postId
        }
        _currentScreen.value = screen
    }

    // Authentication Actions
    fun registerUser(
        name: String,
        studentId: String,
        department: String,
        email: String,
        phone: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.register(name, studentId, department, email, phone)
            if (res.isSuccess) {
                onResult(true, "Account created successfully!")
                navigateTo("home")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email)
            if (res.isSuccess) {
                onResult(true, "Welcome back, ${res.getOrNull()?.name}!")
                navigateTo("home")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.addNotification(
                title = "Password Reset Link Sent",
                body = "A secure reset token has been simulated and sent to $email."
            )
            onResult(true, "Simulated reset email sent to $email.")
        }
    }

    fun updateProfile(
        name: String,
        studentId: String,
        department: String,
        phone: String,
        photoUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.updateProfile(name, studentId, department, phone, photoUrl)
            if (res.isSuccess) {
                onResult(true, "Profile updated successfully!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Profile update failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            navigateTo("auth")
        }
    }

    // Post filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(type: String, category: String) {
        _typeFilter.value = type
        _categoryFilter.value = category
    }

    // Creating post
    fun createPost(
        type: String,
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        location: String,
        locationLatStr: String,
        locationLngStr: String,
        contactInfo: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val gpsStr = if (locationLatStr.isNotBlank() && locationLngStr.isNotBlank()) "$locationLatStr,$locationLngStr" else ""
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val res = repository.createPost(
                type = type,
                title = title,
                description = description,
                category = category,
                imageUrl = imageUrl,
                location = location,
                gpsLocation = gpsStr,
                date = todayDate,
                contactInfo = contactInfo
            )
            if (res.isSuccess) {
                onResult(true, "Post created successfully!")
                navigateTo("home")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to save post")
            }
        }
    }

    fun markPostResolved(postId: String) {
        viewModelScope.launch {
            repository.markPostAsResolved(postId)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            navigateTo("home")
        }
    }

    // Mod/Report functions
    fun reportPost(postId: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.reportPost(postId, reason)
            if (res.isSuccess) {
                onResult(true, "Item reported. Admins will review promptly.")
            } else {
                onResult(false, "Failed to submit report.")
            }
        }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            repository.dismissReport(reportId)
        }
    }

    fun deleteSpamPost(postId: String, reportId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            repository.dismissReport(reportId)
        }
    }

    fun banUserFromCampus(uid: String) {
        viewModelScope.launch {
            repository.banUser(uid)
        }
    }

    // Notification Actions
    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
