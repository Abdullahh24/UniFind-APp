package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.UserEntity
import com.example.ui.viewmodel.UniFindViewModel
import kotlinx.coroutines.delay

// ==========================================
// CENTRAL NAVIGATED LAYOUT
// ==========================================
@Composable
fun UniFindAppContent(viewModel: UniFindViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Scaffold(
        bottomBar = {
            if (currentUser != null && currentScreen != "splash") {
                UniFindBottomBar(
                    currentScreen = currentScreen,
                    isAdmin = currentUser?.role == "admin",
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "splash" -> SplashScreen(viewModel)
                "auth" -> AuthScreen(viewModel)
                "home" -> HomeScreen(viewModel)
                "add_post" -> AddPostScreen(viewModel)
                "post_detail" -> PostDetailScreen(viewModel)
                "my_posts" -> MyPostsScreen(viewModel)
                "profile" -> ProfileScreen(viewModel)
                "admin" -> AdminPanelScreen(viewModel)
                "notifications" -> NotificationsScreen(viewModel)
            }
        }
    }
}

// ==========================================
// SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(viewModel: UniFindViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var appTitleVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        appTitleVisible = true
        delay(400)
        subtitleVisible = true
        delay(1500) // Animated wait
        
        // Auto sign-in or direct authentication landing
        if (currentUser != null) {
            viewModel.navigateTo("home")
        } else {
            viewModel.navigateTo("auth")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "UniFind Logo Shield",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = appTitleVisible,
                enter = fadeIn() + expandVertically()
            ) {
                Text(
                    text = "UniFind",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = subtitleVisible,
                enter = fadeIn()
            ) {
                Text(
                    text = "Campus Lost & Found hub",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "Google AI Studio • Built Securely",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

// ==========================================
// AUTHENTICATION SCREEN (LOGIN/REGISTER)
// ==========================================
@Composable
fun AuthScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(false) }

    // Forms fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    val logoPulse = rememberInfiniteTransition(label = "pulse")
    val scaleFactor by logoPulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Branding shield
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isRegisterMode) "Create Campus Account" else "UniFind Portal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = if (isRegisterMode) "Sign up with your university email" else "Enter credentials or use instant developer tap",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student ID Code") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_studentid_field"),
                        singleLine = true,
                        placeholder = { Text("e.g. STU-4421") }
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department / Faculty") },
                        leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_department_field"),
                        singleLine = true,
                        placeholder = { Text("e.g. Computer Science") }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("University Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    placeholder = { Text("e.g. alex@student.edu") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Account Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Password toggle"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (!isRegisterMode) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    if (email.isBlank()) {
                                        Toast.makeText(context, "Please enter your email first", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.resetPassword(email) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in email and password.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (isRegisterMode) {
                            if (name.isBlank() || studentId.isBlank() || department.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, "Please complete all registration fields profile.", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            viewModel.registerUser(name, studentId, department, email, phone) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            viewModel.loginUser(email) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Register New Student" else "Campus Access Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Toggle Account actions
        Text(
            text = if (isRegisterMode) "Already have an account? Access login here" else "New student? Create your profile",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isRegisterMode = !isRegisterMode }
                .padding(8.dp)
        )

    }
}

// ==========================================
// HOME DASHBOARD SCREEN
// ==========================================
@Composable
fun HomeScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1200) // Simulated network pull-to-refresh
            isRefreshing = false
            Toast.makeText(context, "Campus listings up to date", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo("add_post") },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_item_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Report post",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Dashboard header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "UniFind",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notifications Inbox icon
                    Box(
                        modifier = Modifier.clickable { viewModel.navigateTo("notifications") }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notification Center",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )

                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // JD initials avatar
                    val initials = remember(currentUser) {
                        currentUser?.name?.split(" ")
                            ?.filter { it.isNotEmpty() }
                            ?.map { it.first().uppercase() }
                            ?.take(2)
                            ?.joinToString("") ?: "JD"
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
                            .clickable { viewModel.navigateTo("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search for items...", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(1.dp, CircleShape)
                    .testTag("home_search_bar"),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = CircleShape
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dual filters section
            // A. Type selection filter chips (Lost / Found / All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipListItem(
                    label = "All Items",
                    selected = typeFilter == "all",
                    onClick = { viewModel.updateFilters("all", categoryFilter) }
                )
                FilterChipListItem(
                    label = "🔴 Lost Only",
                    selected = typeFilter == "lost",
                    onClick = { viewModel.updateFilters("lost", categoryFilter) }
                )
                FilterChipListItem(
                    label = "🟢 Found Only",
                    selected = typeFilter == "found",
                    onClick = { viewModel.updateFilters("found", categoryFilter) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // B. Category pill row
            val categories = listOf("All", "Electronics", "Wallet", "Keys", "Book", "Documents", "Other")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = categoryFilter.lowercase() == category.lowercase()
                    AssistChip(
                        onClick = { viewModel.updateFilters(typeFilter, category) },
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Refresh controller / listings lists
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (posts.isEmpty()) {
                    EmptyStatePlaceholder(
                        title = "No Campus Postings Match",
                        subtitle = "Try resetting filters or searching for alternative descriptions. Pull down/tap top to refresh listings cache."
                    )
                } else {
                    val scrollState = rememberScrollState()
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("posts_feed_list")
                    ) {
                        items(posts) { post ->
                            CampusPostCard(
                                post = post,
                                onClick = { viewModel.navigateTo("post_detail", post.postId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipListItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .clickable { onClick() }
            .shadow(if (selected) 2.dp else 0.dp, CircleShape)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// ==========================================
// CAMPUS FEED POST CARD COMPONENT
// ==========================================
@Composable
fun CampusPostCard(post: PostEntity, onClick: () -> Unit) {
    val isLost = post.type.lowercase() == "lost"
    val colorAccent = if (isLost) Color(0xFFB91C1C) else Color(0xFF047857) // red-700 / green-700
    val bgAccent = if (isLost) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)     // red-100 / green-100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("post_card_${post.postId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card image thumbnail (LEFT)
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Category overlaid tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.category,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Card details column (RIGHT)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type badge
                        Box(
                            modifier = Modifier
                                .background(bgAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = post.type.uppercase(),
                                color = colorAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Time/Date post report
                        Text(
                            text = post.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = post.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = post.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = post.location,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Resolved overlay cover
            if (post.status.lowercase() == "resolved") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9).copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "RESOLVED",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REPORT / ADD POST SCREEN
// ==========================================
@Composable
fun AddPostScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var type by remember { mutableStateOf("lost") } // "lost" or "found"
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var imageUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var locationLatStr by remember { mutableStateOf("") }
    var locationLngStr by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
            Toast.makeText(context, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    val categories = listOf("Electronics", "Wallet", "Keys", "Book", "Documents", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        CustomScreenHeader(
            title = "Report Campus Item",
            onBackClick = { viewModel.navigateTo("home") }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selection type (Lost / Found switches)
            Text(
                text = "What type of report is this?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (type == "lost") Color(0xFFE53935) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (type == "lost") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { type = "lost" }
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚨 Report LOST Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (type == "found") Color(0xFF43A047) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (type == "found") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { type = "found" }
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🟢 Report FOUND Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Divider()

            // Form Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Item Title") },
                placeholder = { Text("e.g. Leather wallet, blue lanyard key") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_item_title"),
                singleLine = true
            )

            // Category select
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Item Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedCategoryDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Campus Location") },
                placeholder = { Text("e.g. Hallway of Science Center B, Study Room 22") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("add_item_location"),
                singleLine = true
            )

            // Optional GPS Location parameters
            Text(
                text = "Item GPS Coordinates (Optional / Simulated)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = locationLatStr,
                    onValueChange = { locationLatStr = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("e.g. 40.712") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = locationLngStr,
                    onValueChange = { locationLngStr = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("e.g. -74.008") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Premium Photo Selector
            Text(
                text = "Item Photo Upload",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Uploaded photo preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Row for overlay edit/delete actions
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { imageUrl = "" },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload Photo icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to upload photo from your device",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Or tap a mock sample photo below",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            var showUrlInput by remember { mutableStateOf(false) }

            if (showUrlInput) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Enter Web Image URL manually") },
                    placeholder = { Text("e.g. https://images.unsplash.com/...") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                TextButton(
                    onClick = { showUrlInput = true },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("+ Provide Image URL instead", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // High-res Demo Options
            Text(
                text = "Instant high-res presets:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val demoPhotos = listOf(
                    "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=400&q=80" to "Laptop",
                    "https://images.unsplash.com/photo-1627124765135-56c33fc36eab?auto=format&fit=crop&w=400&q=80" to "Wallet",
                    "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=400&q=80" to "Keys",
                    "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=400&q=80" to "Book"
                )
                demoPhotos.forEach { (url, label) ->
                    AssistChip(
                        onClick = {
                            imageUrl = url
                            Toast.makeText(context, "Preset selected!", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Contact Information") },
                placeholder = { Text("${currentUser?.email} / ${currentUser?.phone}") },
                leadingIcon = { Icon(Icons.Default.ContactPage, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("add_item_contact"),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Items Description & Characteristics") },
                placeholder = { Text("Describe distinct details like colors, brand, serial codes, content inside or protective accessories...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .testTag("add_item_description"),
                maxLines = 4
            )

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Please complete items Title, Description & Location fields", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    viewModel.createPost(
                        type = type,
                        title = title,
                        description = description,
                        category = category,
                        imageUrl = imageUrl,
                        location = location,
                        locationLatStr = locationLatStr,
                        locationLngStr = locationLngStr,
                        contactInfo = contactInfo
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_item_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit Report Posting",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ==========================================
// ITEM DETAILS SCREEN
// ==========================================
@Composable
fun PostDetailScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.allPosts.collectAsState()
    val selectedPostId by viewModel.selectedPostId.collectAsState()

    val post = remember(posts, selectedPostId) {
        posts.find { it.postId == selectedPostId }
    }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Posting information could not be found.")
        }
        return
    }

    val isLost = post.type.lowercase() == "lost"
    val isOwner = post.uid == currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        CustomScreenHeader(
            title = "${post.type.uppercase()} Detail",
            onBackClick = { viewModel.navigateTo("home") }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Visual big banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Large overlay pill for type status
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .background(
                            color = if (isLost) Color(0xFFE53935) else Color(0xFF43A047),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = post.type.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Resolved status cover
                if (post.status.lowercase() == "resolved") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            border = BorderStroke(2.dp, Color(0xFF4CAF50)),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "RESOLVED & RECLAIMED",
                                color = Color(0xFF4CAF50),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Chip / Date information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(post.category) }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Report Date: ${post.date}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Title header
                Text(
                    text = post.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Location row card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Device coordinates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Campus Location Spot",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = post.location,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (post.gpsLocation.isNotBlank()) {
                                Text(
                                    text = "Simulated GPS: ${post.gpsLocation}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Description Title
                Text(
                    text = "Detailed Description",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = post.description,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )

                Divider()

                // Contact info card
                Text(
                    text = "Contact Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = post.contactInfo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ACTIONS PANEL (CONDITIONAL ON ROLE)
                if (isOwner && post.status.lowercase() == "active") {
                    Button(
                        onClick = {
                            viewModel.markPostResolved(post.postId)
                            Toast.makeText(context, "Marked as resolved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mark_resolved_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as CLAIMED / RESOLVED", fontWeight = FontWeight.Bold)
                    }
                }

                if (isOwner || currentUser?.role == "admin") {
                    OutlinedButton(
                        onClick = {
                            viewModel.deletePost(post.postId)
                            Toast.makeText(context, "Listing deleted successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("delete_post_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Listing Permanent")
                    }
                }

                if (!isOwner) {
                    // Contact Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Direct messaging / phone prompt activated for card owner", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Student", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                showReportDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.Report, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report Spam", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // SPAM REPORT OVERWRITE DIALOG
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Flagged Posting") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Help UniFind maintain security. Why is this posting inappropriate?", fontSize = 13.sp)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Spam, offensive details, incorrect campus category...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isBlank()) {
                            Toast.makeText(context, "Provide a reason first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.reportPost(post.postId, reportReason) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showReportDialog = false
                        }
                    }
                ) {
                    Text("Submit Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

// ==========================================
// MY POSTS SCREEN
// ==========================================
@Composable
fun MyPostsScreen(viewModel: UniFindViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()

    val myPosts = remember(allPosts, currentUser) {
        allPosts.filter { it.uid == currentUser?.uid }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomScreenHeader(
            title = "My Campus Reports",
            onBackClick = { viewModel.navigateTo("profile") }
        )

        if (myPosts.isEmpty()) {
            EmptyStatePlaceholder(
                title = "No Submissions Yet",
                subtitle = "Items look empty. If you lose or locate accessories on campus, start typing a request from the home dashboard."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myPosts) { post ->
                    CampusPostCard(
                        post = post,
                        onClick = { viewModel.navigateTo("post_detail", post.postId) }
                    )
                }
            }
        }
    }
}

// ==========================================
// STUDENT PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    // Edit states
    var nameEdit by remember { mutableStateOf(currentUser?.name ?: "") }
    var studentIdEdit by remember { mutableStateOf(currentUser?.studentId ?: "") }
    var departmentEdit by remember { mutableStateOf(currentUser?.department ?: "") }
    var phoneEdit by remember { mutableStateOf(currentUser?.phone ?: "") }

    val userPostCount = remember(allPosts, currentUser) {
        allPosts.count { it.uid == currentUser?.uid }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            nameEdit = it.name
            studentIdEdit = it.studentId
            departmentEdit = it.department
            phoneEdit = it.phone
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Profile Title Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Student Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Avatar Picture
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(2.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = currentUser?.photoUrl ?: "https://api.dicebear.com/7.x/pixel-art/svg?seed=Demo",
                contentDescription = "Profile placeholder avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Divider()

        if (isEditing) {
            // Edit Fields mode
            OutlinedTextField(
                value = nameEdit,
                onValueChange = { nameEdit = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_name"),
                singleLine = true
            )

            OutlinedTextField(
                value = studentIdEdit,
                onValueChange = { studentIdEdit = it },
                label = { Text("Student School ID") },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_id"),
                singleLine = true
            )

            OutlinedTextField(
                value = departmentEdit,
                onValueChange = { departmentEdit = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_dept"),
                singleLine = true
            )

            OutlinedTextField(
                value = phoneEdit,
                onValueChange = { phoneEdit = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_phone"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (nameEdit.isBlank() || studentIdEdit.isBlank() || departmentEdit.isBlank()) {
                            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateProfile(
                            name = nameEdit,
                            studentId = studentIdEdit,
                            department = departmentEdit,
                            phone = phoneEdit,
                            photoUrl = currentUser?.photoUrl ?: ""
                        ) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (ok) isEditing = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Changes")
                }

                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        } else {
            // Read-Only Student Badge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OFFICIAL DIGITAL MIT IDENTIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (currentUser?.role == "admin") Color(0xFFD68A1A) else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentUser?.role?.uppercase() ?: "USER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Divider()

                    Text(
                        text = currentUser?.name ?: "Unknown Peer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("STUDENT ID", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(currentUser?.studentId ?: "Not Provided", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DEPARTMENT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(currentUser?.department ?: "Not Provided", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UNIVERSITY EMAIL", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(currentUser?.email ?: "alex@student.edu", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TELEPHONE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(currentUser?.phone ?: "Not Provided", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { isEditing = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modify Profile", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo("my_posts") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("My Postings ($userPostCount)", fontSize = 12.sp, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // System Settings Options List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dark Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text("Varsity Night Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Toggle high-contrast dark theme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }

                // Security Policy Disclaimer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFD68A1A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("University Student Safety Code", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Campus security policies strictly enforce the return of student items. Spammers, banned items and offenders will be restricted.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Log out button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Secure Terminal Session Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ==========================================
// CAMPUS ALERTS / NOTIFICATION SCREEN
// ==========================================
@Composable
fun NotificationsScreen(viewModel: UniFindViewModel) {
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomScreenHeader(
            title = "Campus Bulletin Alerts",
            onBackClick = { viewModel.navigateTo("home") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notifications.size} Real-time Alert Messages",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (notifications.isNotEmpty()) {
                TextButton(
                    onClick = {
                        viewModel.clearNotifications()
                        Toast.makeText(context, "Alert inbox cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Inbox", fontSize = 12.sp)
                }
            }
        }

        if (notifications.isEmpty()) {
            EmptyStatePlaceholder(
                title = "Notification Inbox Empty",
                subtitle = "FCM simulated alert streams will appear here when items are reported lost or discovered on campus."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.markNotificationRead(alert.notificationId) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (alert.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (alert.isRead) Icons.Default.MailOutline else Icons.Default.MarkAsUnread,
                                    contentDescription = null,
                                    tint = if (alert.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.body,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SERVICE ADMIN PANEL SCREEN
// ==========================================
@Composable
fun AdminPanelScreen(viewModel: UniFindViewModel) {
    val context = LocalContext.current
    val allReports by viewModel.allReports.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()

    var activeTab by remember { mutableStateOf("reports") } // "reports" or "users"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        CustomScreenHeader(
            title = "Chancellor Safety Panel",
            onBackClick = { viewModel.navigateTo("home") }
        )

        // TAB SWITCHERS
        TabRow(
            selectedTabIndex = if (activeTab == "reports") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = activeTab == "reports",
                onClick = { activeTab = "reports" },
                text = { Text("Spam Reports (${allReports.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == "users",
                onClick = { activeTab = "users" },
                text = { Text("Registered Students (${usersList.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (activeTab == "reports") {
            // SPAM LIST MODERATION
            if (allReports.isEmpty()) {
                EmptyStatePlaceholder(
                    title = "Database Clean: 0 Spam Reports",
                    subtitle = "Congratulations! There are no offensive items or reports flagged by students."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allReports) { r ->
                        val post = allPosts.find { it.postId == r.postId }
                        val reporter = usersList.find { it.uid == r.reportedBy }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = r.reason,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                
                                Text(
                                    text = "Reported By: ${reporter?.name ?: "Student"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (post != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = post.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = post.description,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                } else {
                                    Text("Post details already deleted.", fontSize = 11.sp, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.deleteSpamPost(r.postId, r.reportId)
                                            Toast.makeText(context, "Listing permanently expunged", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delete Item", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.dismissReport(r.reportId)
                                            Toast.makeText(context, "Alert report cleared on student database", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Keep Listing", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // USERS LISTING MODERATION
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(usersList) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "ID: ${student.studentId} • ${student.department}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            if (student.role != "admin") {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.banUserFromCampus(student.uid)
                                        Toast.makeText(context, "${student.name} account deactivated successfully.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                    border = BorderStroke(1.dp, Color(0xFFE53935))
                                ) {
                                    Text("Deactivate", fontSize = 11.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFD68A1A).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN", color = Color(0xFFD68A1A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SHARED DESIGN HOOKS & COMPILATION CARDS
// ==========================================
@Composable
fun CustomScreenHeader(title: String, onBackClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Return",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun EmptyStatePlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(68.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}

// BOTTOM SYSTEM NAVIGATION BAR
@Composable
fun UniFindBottomBar(
    currentScreen: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentScreen == "home" || currentScreen == "post_detail",
                onClick = { onNavigate("home") },
                icon = { Icon(if (currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Dashboard") },
                label = { Text("Feed", fontWeight = if (currentScreen == "home" || currentScreen == "post_detail") FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )

            NavigationBarItem(
                selected = currentScreen == "notifications",
                onClick = { onNavigate("notifications") },
                icon = { Icon(if (currentScreen == "notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications, contentDescription = "Campus Alerts") },
                label = { Text("Notices", fontWeight = if (currentScreen == "notifications") FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )

            NavigationBarItem(
                selected = currentScreen == "profile" || currentScreen == "my_posts",
                onClick = { onNavigate("profile") },
                icon = { Icon(if (currentScreen == "profile") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Official card ID") },
                label = { Text("Profile", fontWeight = if (currentScreen == "profile" || currentScreen == "my_posts") FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )

            if (isAdmin) {
                NavigationBarItem(
                    selected = currentScreen == "admin",
                    onClick = { onNavigate("admin") },
                    icon = { Icon(if (currentScreen == "admin") Icons.Filled.Security else Icons.Outlined.Security, contentDescription = "Admin Area") },
                    label = { Text("Security", fontWeight = if (currentScreen == "admin") FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}
