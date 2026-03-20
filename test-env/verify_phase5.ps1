$baseUrl = "http://127.0.0.1:8081/api"

function Invoke-Api([string]$uri, [string]$method, $body, $headers = @{}) {
    try {
        if ($body) {
            return Invoke-RestMethod -Uri $uri -Method $method -Body $body -ContentType "application/json" -Headers $headers
        } else {
            return Invoke-RestMethod -Uri $uri -Method $method -Headers $headers
        }
    } catch {
        if ($_.Exception.Response) {
            $streamReader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $streamReader.ReadToEnd()
            Write-Host "ERROR ($($_.Exception.Response.StatusCode)): $errorBody" -ForegroundColor Red
        } else {
            Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        }
        return $null
    }
}

# 1. Register and Login User 1
$u1Email = "test_$((Get-Date).Ticks)@gmail.com"
$reg1Body = @{ name = "Test User"; email = $u1Email; password = "123456" } | ConvertTo-Json
$reg1Res = Invoke-Api -uri "$baseUrl/auth/register" -method Post -body $reg1Body
if (!$reg1Res) { exit 1 }

$loginBody = @{ email = $u1Email; password = "123456" } | ConvertTo-Json
$loginRes = Invoke-Api -uri "$baseUrl/auth/login" -method Post -body $loginBody
if (!$loginRes) { exit 1 }
$token = $loginRes.token
$userId = $loginRes.userId
Write-Host "Login Successful ($u1Email). UserID: $userId"

# 2. Create Room
$createRoomBody = @{ name = "Phase 5 Room"; language = "javascript" } | ConvertTo-Json
$createRes = Invoke-Api -uri "$baseUrl/rooms/create" -method Post -body $createRoomBody -headers @{ Authorization = "Bearer $token" }
if (!$createRes) { exit 1 }
$roomCode = $createRes.roomCode
Write-Host "Room Created: $roomCode"

# 3. Get Chat History
$chatRes = Invoke-Api -uri "$baseUrl/chat/$roomCode/messages" -method Get -headers @{ Authorization = "Bearer $token" }
Write-Host "Chat History (Initial): $($chatRes | ConvertTo-Json -Compress)"

# 4. Get My Role
$roleRes = Invoke-Api -uri "$baseUrl/roles/$roomCode/my-role" -method Get -headers @{ Authorization = "Bearer $token" }
Write-Host "My Role (Owner): $roleRes"

# 5. Register User 2
$u2Email = "user2_$((Get-Date).Ticks)@gmail.com"
$reg2Body = @{ name = "User Two"; email = $u2Email; password = "123456" } | ConvertTo-Json
$reg2Res = Invoke-Api -uri "$baseUrl/auth/register" -method Post -body $reg2Body
if (!$reg2Res) { exit 1 }
$token2 = $reg2Res.token
$userId2 = $reg2Res.user.id
Write-Host "User 2 Registered ($u2Email). UserID: $userId2"

# 4. User 2 Join Room
$u2JoinRes = Invoke-Api -uri "$baseUrl/rooms/join/$roomCode" -method Post -headers @{ Authorization = "Bearer $token2" }
if (!$u2JoinRes) { exit 1 }
# Extract User 2's ID from the participants list
$u2UserId = $u2JoinRes.participants | Where-Object { $_.email -eq $u2Email } | Select-Object -ExpandProperty userId
Write-Host "User 2 Joined Room. UserID: $u2UserId"

# 5. Change Role (Owner changes User 2 to VIEWER)
$changeRoleBody = @{ targetUserId = $u2UserId; newRole = "VIEWER" } | ConvertTo-Json
$changeRes = Invoke-Api -uri "$baseUrl/roles/$roomCode/change" -method Post -body $changeRoleBody -headers @{ Authorization = "Bearer $token" }
Write-Host "Change Role Result: $($changeRes)"

# 6. Verify Role Change (User 2 checks their role)
$u2RoleRes = Invoke-Api -uri "$baseUrl/roles/$roomCode/my-role" -method Get -headers @{ Authorization = "Bearer $token2" }
Write-Host "User 2 Role: $u2RoleRes"
