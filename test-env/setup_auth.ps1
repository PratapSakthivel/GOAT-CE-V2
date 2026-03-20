$regBody = @{
    name = "Test User"
    email = "test6@gmail.com"
    password = "123456"
} | ConvertTo-Json

$regResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
Write-Output "Registration: Success"

$loginBody = @{
    email = "test6@gmail.com"
    password = "123456"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Output "Token: $token"
