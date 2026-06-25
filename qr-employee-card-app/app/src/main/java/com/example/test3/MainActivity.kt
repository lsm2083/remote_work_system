package com.example.test3

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test3.ui.theme.Test3Theme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// 백엔드(C:\workspace\Flask) 서버 주소. 서버 IP가 바뀌면 여기만 수정하면 된다.
private const val BASE_URL = "http://192.168.0.20:5000"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AuthApp(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("로그인 정보를 입력하세요.") }

    var loggedInName by remember { mutableStateOf("") }
    var loggedInInfo by remember { mutableStateOf("") }
    var loggedInEmployeeId by remember { mutableStateOf("") }
    var loggedInEmail by remember { mutableStateOf("") }
    var loggedInDepartment by remember { mutableStateOf("") }
    var loggedInPosition by remember { mutableStateOf("") }
    var loggedInRole by remember { mutableStateOf("") }
    var loggedInPhotoUrl by remember { mutableStateOf("") }
    var loggedInToken by remember { mutableStateOf("") }   // [추가] JWT
    var qrData by remember { mutableStateOf("") }

    var loginId by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }

    var signName by rememberSaveable { mutableStateOf("") }
    var signEmail by rememberSaveable { mutableStateOf("") }
    var signPhone by rememberSaveable { mutableStateOf("") }
    var signDepartment by rememberSaveable { mutableStateOf("") }
    var signPosition by rememberSaveable { mutableStateOf("") }
    var signPassword by rememberSaveable { mutableStateOf("") }
    var signPasswordConfirm by rememberSaveable { mutableStateOf("") }

    var emailChecked by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Remote Work System",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("상태", style = MaterialTheme.typography.titleMedium)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (loggedInName.isNotBlank()) {
            // [추가] 로그인 후 홈: 사원증 / 공지사항 / 내 정보 탭
            var homeTab by rememberSaveable { mutableIntStateOf(0) }

            TabRow(selectedTabIndex = homeTab) {
                Tab(homeTab == 0, { homeTab = 0 }, text = { Text("사원증") })
                Tab(homeTab == 1, { homeTab = 1 }, text = { Text("공지사항") })
                Tab(homeTab == 2, { homeTab = 2 }, text = { Text("게시판") })
                Tab(homeTab == 3, { homeTab = 3 }, text = { Text("내 정보") })
            }

            when (homeTab) {
                0 -> EmployeeIdCard(
                    name = loggedInName,
                    employeeId = loggedInEmployeeId,
                    email = loggedInEmail,
                    department = loggedInDepartment,
                    position = loggedInPosition,
                    role = loggedInRole,
                    photoUrl = loggedInPhotoUrl,
                    qrData = qrData
                )
                1 -> NoticeScreen(token = loggedInToken)
                2 -> BoardScreen(token = loggedInToken)
                3 -> ProfileScreen(token = loggedInToken)
            }

            Button(
                onClick = {
                    loggedInName = ""
                    loggedInInfo = ""
                    loggedInEmployeeId = ""
                    loggedInEmail = ""
                    loggedInDepartment = ""
                    loggedInPosition = ""
                    loggedInRole = ""
                    loggedInPhotoUrl = ""
                    loggedInToken = ""
                    qrData = ""

                    loginId = ""
                    loginPassword = ""

                    selectedTab = 0
                    message = "로그아웃되었습니다."
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                enabled = !loading
            ) {
                Text("로그아웃")
            }
        } else {
            Text(
                text = "로그인 / 회원가입",
                style = MaterialTheme.typography.titleMedium
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("로그인") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("회원가입") }
                )
            }

            if (selectedTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("로그인", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = loginId,
                            onValueChange = { loginId = it },
                            label = { Text("이메일 또는 사번") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("비밀번호") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Button(
                            onClick = {
                                if (loginId.isBlank()) {
                                    message = "이메일 또는 사번을 입력하세요."
                                    return@Button
                                }

                                if (loginPassword.isBlank()) {
                                    message = "비밀번호를 입력하세요."
                                    return@Button
                                }

                                scope.launch {
                                    loading = true
                                    message = "로그인 요청 중..."

                                    val result = loginRequest(
                                        loginId = loginId,
                                        password = loginPassword
                                    )

                                    loading = false
                                    message = result.message

                                    if (result.success) {
                                        loggedInName = result.name
                                        loggedInInfo = result.info
                                        qrData = result.qrData

                                        loggedInEmployeeId = result.employeeId
                                        loggedInEmail = result.email
                                        loggedInDepartment = result.department
                                        loggedInPosition = result.position
                                        loggedInRole = result.role
                                        loggedInPhotoUrl = result.photoUrl
                                        loggedInToken = result.token
                                    } else {
                                        loggedInName = ""
                                        loggedInInfo = ""
                                        qrData = ""

                                        loggedInEmployeeId = ""
                                        loggedInEmail = ""
                                        loggedInDepartment = ""
                                        loggedInPosition = ""
                                        loggedInRole = ""
                                        loggedInPhotoUrl = ""
                                        loggedInToken = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            enabled = !loading
                        ) {
                            if (loading) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Text("로그인")
                            }
                        }

                        TextButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {
                            Text("회원가입 하러가기")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("회원가입", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = signName,
                            onValueChange = { signName = it },
                            label = { Text("이름") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = signEmail,
                            onValueChange = {
                                signEmail = it
                                emailChecked = false
                            },
                            label = { Text("이메일") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Button(
                            onClick = {
                                if (signEmail.isBlank()) {
                                    message = "이메일을 입력하세요."
                                    return@Button
                                }

                                scope.launch {
                                    loading = true
                                    message = "이메일 중복확인 중..."

                                    val result = checkEmailDuplicateRequest(
                                        email = signEmail
                                    )

                                    loading = false

                                    if (result.success) {
                                        emailChecked = true
                                        message = "사용 가능한 이메일입니다."
                                    } else {
                                        emailChecked = false
                                        message = result.message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {
                            Text(if (emailChecked) "이메일 확인 완료" else "이메일 중복확인")
                        }

                        OutlinedTextField(
                            value = signPhone,
                            onValueChange = { signPhone = it },
                            label = { Text("전화번호") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = signDepartment,
                            onValueChange = { signDepartment = it },
                            label = { Text("부서") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = signPosition,
                            onValueChange = { signPosition = it },
                            label = { Text("직급") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = signPassword,
                            onValueChange = { signPassword = it },
                            label = { Text("비밀번호") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = signPasswordConfirm,
                            onValueChange = { signPasswordConfirm = it },
                            label = { Text("비밀번호 확인") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Button(
                            onClick = {
                                if (signName.isBlank()) {
                                    message = "이름을 입력하세요."
                                    return@Button
                                }

                                if (signEmail.isBlank()) {
                                    message = "이메일을 입력하세요."
                                    return@Button
                                }

                                if (signPassword.isBlank()) {
                                    message = "비밀번호를 입력하세요."
                                    return@Button
                                }

                                if (signPassword != signPasswordConfirm) {
                                    message = "비밀번호 확인이 일치하지 않습니다."
                                    return@Button
                                }

                                if (!emailChecked) {
                                    message = "이메일 중복확인을 먼저 해주세요."
                                    return@Button
                                }

                                scope.launch {
                                    loading = true
                                    message = "회원가입 요청 중..."

                                    val result = registerRequest(
                                        name = signName,
                                        email = signEmail,
                                        phone = signPhone,
                                        department = signDepartment,
                                        position = signPosition,
                                        password = signPassword
                                    )

                                    loading = false
                                    message = result.message

                                    if (result.success) {
                                        loginId = signEmail
                                        loginPassword = ""

                                        signName = ""
                                        signEmail = ""
                                        signPhone = ""
                                        signDepartment = ""
                                        signPosition = ""
                                        signPassword = ""
                                        signPasswordConfirm = ""

                                        emailChecked = false

                                        selectedTab = 0
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            enabled = !loading
                        ) {
                            if (loading) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Text("회원가입 완료")
                            }
                        }

                        TextButton(
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {
                            Text("로그인 하러가기")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun EmployeeIdCard(
    name: String,
    employeeId: String,
    email: String,
    department: String,
    position: String,
    role: String,
    photoUrl: String,
    qrData: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "사원증",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(145.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotBlank() && photoUrl != "null") {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "사원 사진",
                        modifier = Modifier
                            .size(145.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (name.isNotBlank()) name.take(1) else "?",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (qrData.isNotBlank()) {
                val qrBitmap = remember(qrData) {
                    createQrBitmap(qrData, 700)
                }

                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "사원 QR 코드",
                    modifier = Modifier.size(190.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmployeeInfoRow("이름", name)
                    EmployeeInfoRow("사번", employeeId)
                    EmployeeInfoRow("이메일", email)
                    EmployeeInfoRow("부서", department)
                    EmployeeInfoRow("직급", position)
                    EmployeeInfoRow("권한", role)
                }
            }
        }
    }
}

@Composable
fun EmployeeInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (value.isBlank() || value == "null") "-" else value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class AuthResult(
    val success: Boolean,
    val message: String,
    val name: String = "",
    val info: String = "",
    val qrData: String = "",
    val employeeId: String = "",
    val email: String = "",
    val department: String = "",
    val position: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val token: String = ""   // [추가] 로그인 시 발급되는 JWT (이후 인증 API 호출용)
)

// [추가] 공지사항 한 건
data class NoticeItem(
    val title: String,
    val content: String,
    val author: String,
    val createdAt: String,
    val pinned: Boolean
)

// [추가] 내 프로필
data class ProfileData(
    val name: String,
    val employeeId: String,
    val email: String,
    val phone: String,
    val department: String,
    val position: String,
    val role: String,
    val createdAt: String
)

suspend fun registerRequest(
    name: String,
    email: String,
    phone: String,
    department: String,
    position: String,
    password: String
): AuthResult = withContext(Dispatchers.IO) {
    try {
        val body = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("phone", phone)
            put("department", department)
            put("position", position)
            put("password", password)
        }

        val response = requestPost("/app/register", body)

        AuthResult(
            success = response.optBoolean("success"),
            message = response.optString("message", "회원가입 응답을 받았습니다.")
        )
    } catch (t: Throwable) {
        AuthResult(
            success = false,
            message = "${t.javaClass.simpleName}: ${t.message}"
        )
    }
}

suspend fun checkEmailDuplicateRequest(
    email: String
): AuthResult = withContext(Dispatchers.IO) {
    try {
        val path = "/app/check-duplicate?email=${URLEncoder.encode(email, "UTF-8")}"
        val response = requestGet(path)

        AuthResult(
            success = response.optBoolean("success"),
            message = response.optString("message", "중복확인 완료")
        )
    } catch (t: Throwable) {
        AuthResult(
            success = false,
            message = "${t.javaClass.simpleName}: ${t.message}"
        )
    }
}

suspend fun loginRequest(
    loginId: String,
    password: String
): AuthResult = withContext(Dispatchers.IO) {
    try {
        val body = JSONObject().apply {
            put("id", loginId)
            put("pw", password)
        }

        val response = requestPost("/app/login", body)
        val employee = response.optJSONObject("employee") ?: response.optJSONObject("user")

        if (response.optBoolean("success") && employee != null) {
            val name = employee.optString("name")
            val employeeId = employee.optString("employee_id")
            val email = employee.optString("email")
            val department = employee.optString("department")
            val position = employee.optString("position")
            val role = employee.optString("role")
            val photoUrl = employee.optString("photo_url", employee.optString("photo"))

            val info = buildString {
                append("사번: $employeeId\n")
                append("이메일: $email\n")
                append("부서: $department\n")
                append("직급: $position\n")
                append("권한: $role")
            }

            var qrData = employee.optString("qr_data")

            if (qrData.isBlank() || qrData == "null") {
                qrData = JSONObject().apply {
                    put("type", "employee_access")
                    put("employee_id", employeeId)
                    put("email", email)
                    put("name", name)
                }.toString()
            }

            AuthResult(
                success = true,
                message = response.optString("message", "로그인 성공"),
                name = name,
                info = info,
                qrData = qrData,
                employeeId = employeeId,
                email = email,
                department = department,
                position = position,
                role = role,
                photoUrl = photoUrl,
                token = response.optString("token")   // [추가] JWT 저장
            )
        } else {
            AuthResult(
                success = false,
                message = response.optString("message", "로그인 실패")
            )
        }
    } catch (t: Throwable) {
        AuthResult(
            success = false,
            message = "${t.javaClass.simpleName}: ${t.message}"
        )
    }
}

fun requestPost(path: String, body: JSONObject, token: String? = null): JSONObject {
    val conn = (URL("$BASE_URL$path").openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 5000
        readTimeout = 5000
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        setRequestProperty("Accept", "application/json")
        // [추가] 토큰이 있으면 JWT 인증 헤더 추가 (게시판 작성/댓글 등 login_required)
        if (!token.isNullOrBlank()) {
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    try {
        conn.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        val text = if (code in 200..299) {
            conn.inputStream.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }
        } else {
            conn.errorStream?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: """{"success":false,"message":"HTTP $code"}"""
        }

        return try {
            JSONObject(text)
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("result", "fail")
                put("message", text)
            }
        }
    } finally {
        conn.disconnect()
    }
}

fun requestGet(path: String, token: String? = null): JSONObject {
    val conn = (URL("$BASE_URL$path").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 5000
        readTimeout = 5000
        setRequestProperty("Accept", "application/json")
        // [추가] 토큰이 있으면 JWT 인증 헤더 추가 (공지/프로필 등 login_required API)
        if (!token.isNullOrBlank()) {
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    try {
        val code = conn.responseCode
        val text = if (code in 200..299) {
            conn.inputStream.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }
        } else {
            conn.errorStream?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: """{"success":false,"message":"HTTP $code"}"""
        }

        return try {
            JSONObject(text)
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("message", text)
            }
        }
    } finally {
        conn.disconnect()
    }
}

fun createQrBitmap(text: String, size: Int): Bitmap {
    val bitMatrix = QRCodeWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            )
        }
    }

    return bitmap
}

// ──────────────────────────────────────────────────────────────
// [추가] 공지사항 화면
// ──────────────────────────────────────────────────────────────
@Composable
fun NoticeScreen(token: String) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var notices by remember { mutableStateOf(listOf<NoticeItem>()) }

    LaunchedEffect(token) {
        loading = true
        error = ""
        val (err, list) = fetchNotices(token)
        error = err ?: ""
        notices = list
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("공지사항", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        when {
            loading -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp)) }

            error.isNotBlank() -> Text("오류: $error", style = MaterialTheme.typography.bodyMedium)

            notices.isEmpty() -> Text("등록된 공지가 없습니다.", style = MaterialTheme.typography.bodyMedium)

            else -> notices.forEach { n ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (n.pinned) "📌 ${n.title}" else n.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = n.content, style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (n.author.isBlank() || n.author == "null") "" else n.author,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = n.createdAt.take(16),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// [추가] 내 정보(프로필) 화면
// ──────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(token: String) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf<ProfileData?>(null) }

    LaunchedEffect(token) {
        loading = true
        error = ""
        val (err, data) = fetchMyProfile(token)
        error = err ?: ""
        profile = data
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("내 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        when {
            loading -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp)) }

            error.isNotBlank() -> Text("오류: $error", style = MaterialTheme.typography.bodyMedium)

            profile != null -> Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val p = profile!!
                    EmployeeInfoRow("이름", p.name)
                    EmployeeInfoRow("사번", p.employeeId)
                    EmployeeInfoRow("이메일", p.email)
                    EmployeeInfoRow("전화번호", p.phone)
                    EmployeeInfoRow("부서", p.department)
                    EmployeeInfoRow("직급", p.position)
                    EmployeeInfoRow("권한", p.role)
                    EmployeeInfoRow("가입일", p.createdAt.take(10))
                }
            }
        }
    }
}

// ── 공지사항 조회 (GET /app/notices, JWT 필요) ───────────────
suspend fun fetchNotices(token: String): Pair<String?, List<NoticeItem>> = withContext(Dispatchers.IO) {
    try {
        val res = requestGet("/app/notices", token)
        if (!res.optBoolean("success")) {
            return@withContext Pair(res.optString("message", "공지사항을 불러오지 못했습니다."), emptyList())
        }
        val arr = res.optJSONArray("notices") ?: return@withContext Pair(null, emptyList())
        val list = ArrayList<NoticeItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                NoticeItem(
                    title = o.optString("title"),
                    content = o.optString("content"),
                    author = o.optString("author_name"),
                    createdAt = o.optString("created_at"),
                    pinned = o.optInt("is_pinned") == 1
                )
            )
        }
        Pair(null, list)
    } catch (t: Throwable) {
        Pair("${t.javaClass.simpleName}: ${t.message}", emptyList())
    }
}

// ── 내 프로필 조회 (GET /me, JWT 필요) ───────────────────────
suspend fun fetchMyProfile(token: String): Pair<String?, ProfileData?> = withContext(Dispatchers.IO) {
    try {
        val res = requestGet("/api/profile/me", token)
        val emp = res.optJSONObject("employee")
            ?: res.optJSONObject("user")
            ?: return@withContext Pair(res.optString("message", "내 정보를 불러오지 못했습니다."), null)

        val data = ProfileData(
            name = emp.optString("name"),
            employeeId = emp.optString("employee_id"),
            email = emp.optString("email"),
            phone = emp.optString("phone"),
            department = emp.optString("department"),
            position = emp.optString("position"),
            role = emp.optString("role"),
            createdAt = emp.optString("created_at")
        )
        Pair(null, data)
    } catch (t: Throwable) {
        Pair("${t.javaClass.simpleName}: ${t.message}", null)
    }
}

// ──────────────────────────────────────────────────────────────
// [추가] 게시판 (React 게시판과 동일한 /api/boards 연동)
// ──────────────────────────────────────────────────────────────
data class BoardItem(
    val id: Int,
    val title: String,
    val category: String,
    val author: String,
    val createdAt: String,
    val viewCount: Int,
    val commentCount: Int
)

data class BoardComment(
    val author: String,
    val content: String,
    val createdAt: String
)

data class BoardDetail(
    val id: Int,
    val title: String,
    val content: String,
    val category: String,
    val author: String,
    val createdAt: String,
    val viewCount: Int,
    val comments: List<BoardComment>
)

@Composable
fun BoardScreen(token: String) {
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var writing by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }

    when {
        writing -> BoardWriteScreen(
            token = token,
            onDone = { writing = false; reload++ },
            onCancel = { writing = false }
        )
        selectedId != null -> BoardDetailScreen(
            token = token,
            boardId = selectedId!!,
            onBack = { selectedId = null; reload++ }
        )
        else -> BoardListScreen(
            token = token,
            reloadKey = reload,
            onOpen = { selectedId = it },
            onWrite = { writing = true }
        )
    }
}

@Composable
fun BoardListScreen(token: String, reloadKey: Int, onOpen: (Int) -> Unit, onWrite: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<BoardItem>()) }

    LaunchedEffect(token, reloadKey) {
        loading = true; error = ""
        val (err, list) = fetchBoards(token)
        error = err ?: ""; items = list; loading = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("게시판", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = onWrite) { Text("✏️ 새 글") }
        }

        when {
            loading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
            error.isNotBlank() -> Text("오류: $error", style = MaterialTheme.typography.bodyMedium)
            items.isEmpty() -> Text("게시글이 없습니다.", style = MaterialTheme.typography.bodyMedium)
            else -> items.forEach { b ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(b.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "[${b.category}] ${b.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (b.author.isBlank() || b.author == "null") "" else b.author,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "💬 ${b.commentCount}  👁 ${b.viewCount}  ${b.createdAt.take(10)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoardDetailScreen(token: String, boardId: Int, onBack: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf<BoardDetail?>(null) }
    var commentText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token, boardId, reload) {
        loading = true; error = ""
        val (err, data) = fetchBoardDetail(token, boardId)
        error = err ?: ""; detail = data; loading = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← 목록으로") }

        when {
            loading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
            error.isNotBlank() -> Text("오류: $error", style = MaterialTheme.typography.bodyMedium)
            detail != null -> {
                val d = detail!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("[${d.category}] ${d.title}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${d.author}  ·  ${d.createdAt.take(16)}  ·  조회 ${d.viewCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(d.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Text("댓글 ${d.comments.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                d.comments.forEach { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (c.author.isBlank()) "" else c.author, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(c.createdAt.take(16), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(c.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("댓글 입력") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (commentText.isBlank()) return@Button
                        scope.launch {
                            sending = true
                            val (ok, _) = createComment(token, boardId, commentText)
                            sending = false
                            if (ok) { commentText = ""; reload++ }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !sending
                ) { Text(if (sending) "등록 중..." else "댓글 등록") }
            }
        }
    }
}

@Composable
fun BoardWriteScreen(token: String, onDone: () -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("일반") }
    var content by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("새 글 작성", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("카테고리") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth())

        if (error.isNotBlank()) Text("오류: $error", style = MaterialTheme.typography.bodySmall)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel, enabled = !sending, modifier = Modifier.weight(1f)) { Text("취소") }
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) { error = "제목과 내용을 입력하세요."; return@Button }
                    scope.launch {
                        sending = true; error = ""
                        val (ok, msg) = createBoard(token, title, content, category)
                        sending = false
                        if (ok) onDone() else error = msg
                    }
                },
                enabled = !sending,
                modifier = Modifier.weight(1f)
            ) { Text(if (sending) "등록 중..." else "등록") }
        }
    }
}

// ── 게시판 목록 (GET /api/boards) ────────────────────────────
suspend fun fetchBoards(token: String): Pair<String?, List<BoardItem>> = withContext(Dispatchers.IO) {
    try {
        val res = requestGet("/api/boards", token)
        if (!res.optBoolean("success")) return@withContext Pair(res.optString("message", "게시글을 불러오지 못했습니다."), emptyList())
        val arr = res.optJSONArray("boards") ?: return@withContext Pair(null, emptyList())
        val list = ArrayList<BoardItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                BoardItem(
                    id = o.optInt("board_id"),
                    title = o.optString("title"),
                    category = o.optString("category", "일반"),
                    author = o.optString("author_name"),
                    createdAt = o.optString("created_at"),
                    viewCount = o.optInt("view_count"),
                    commentCount = o.optInt("comment_count")
                )
            )
        }
        Pair(null, list)
    } catch (t: Throwable) {
        Pair("${t.javaClass.simpleName}: ${t.message}", emptyList())
    }
}

// ── 게시글 상세 + 댓글 (GET /api/boards/{id}) ────────────────
suspend fun fetchBoardDetail(token: String, boardId: Int): Pair<String?, BoardDetail?> = withContext(Dispatchers.IO) {
    try {
        val res = requestGet("/api/boards/$boardId", token)
        val b = res.optJSONObject("board")
            ?: return@withContext Pair(res.optString("message", "게시글을 불러오지 못했습니다."), null)

        val commentsArr = res.optJSONArray("comments")
        val comments = ArrayList<BoardComment>()
        if (commentsArr != null) {
            for (i in 0 until commentsArr.length()) {
                val c = commentsArr.getJSONObject(i)
                comments.add(
                    BoardComment(
                        author = c.optString("author_name"),
                        content = c.optString("content"),
                        createdAt = c.optString("created_at")
                    )
                )
            }
        }

        val detail = BoardDetail(
            id = b.optInt("board_id"),
            title = b.optString("title"),
            content = b.optString("content"),
            category = b.optString("category", "일반"),
            author = b.optString("author_name"),
            createdAt = b.optString("created_at"),
            viewCount = b.optInt("view_count"),
            comments = comments
        )
        Pair(null, detail)
    } catch (t: Throwable) {
        Pair("${t.javaClass.simpleName}: ${t.message}", null)
    }
}

// ── 게시글 작성 (POST /api/boards) ───────────────────────────
suspend fun createBoard(token: String, title: String, content: String, category: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    try {
        val body = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("category", category.ifBlank { "일반" })
        }
        val res = requestPost("/api/boards", body, token)
        Pair(res.optBoolean("success"), res.optString("message", "등록 결과를 받았습니다."))
    } catch (t: Throwable) {
        Pair(false, "${t.javaClass.simpleName}: ${t.message}")
    }
}

// ── 댓글 작성 (POST /api/boards/{id}/comments) ───────────────
suspend fun createComment(token: String, boardId: Int, content: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    try {
        val body = JSONObject().apply { put("content", content) }
        val res = requestPost("/api/boards/$boardId/comments", body, token)
        Pair(res.optBoolean("success"), res.optString("message", "등록 결과를 받았습니다."))
    } catch (t: Throwable) {
        Pair(false, "${t.javaClass.simpleName}: ${t.message}")
    }
}