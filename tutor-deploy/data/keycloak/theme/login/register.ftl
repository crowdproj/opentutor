<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register to opentutor</title>
    <link rel="icon" href="${url.resourcesPath}/favicon.ico" type="image/x-icon">
    <link href="${url.resourcesPath}/css/bootstrap.min.css" rel="stylesheet">
    <link href="${url.resourcesPath}/css/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        .login-container {
            max-width: 500px;
            margin: 0 auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        .login-header {
            text-align: center;
            margin-bottom: 20px;
        }

        .login-header h1 {
            font-size: 1.5rem;
        }

        .footer {
            text-align: center;
            margin-top: 20px;
            font-size: 0.9rem;
            color: #666;
        }

        .footer a {
            color: #666;
            text-decoration: none;
        }

        .footer a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="login-container">
        <div class="login-header">
            <h1>Register to opentutor</h1>
            <p class="subtitle"><span class="required">*</span> Required fields</p>
        </div>
        <form id="kc-register-form" class="form-horizontal" action="${url.registrationAction}" method="post">
            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" name="email" class="form-control" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <input type="password" id="password" name="password" class="form-control" required>
                    <button class="btn btn-outline-secondary" type="button" aria-label="Show password"
                            aria-controls="password" id="toggle-password">
                        <i class="bi bi-eye" aria-hidden="true"></i>
                    </button>
                </div>
            </div>
            <div class="mb-3">
                <label for="password-confirm" class="form-label">Confirm password</label>
                <div class="input-group">
                    <input type="password" id="password-confirm" name="password-confirm" class="form-control" required>
                    <button class="btn btn-outline-secondary" type="button" aria-label="Show password"
                            aria-controls="password-confirm" id="toggle-password-confirm">
                        <i class="bi bi-eye" aria-hidden="true"></i>
                    </button>
                </div>
            </div>
            <div class="mb-3">
                <label for="firstName" class="form-label">First name</label>
                <input type="text" id="firstName" name="firstName" class="form-control" required>
            </div>
            <div class="mb-3">
                <label for="lastName" class="form-label">Last name</label>
                <input type="text" id="lastName" name="lastName" class="form-control" required>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <a href="${url.loginUrl}">« Back to Login</a>
            </div>
            <div class="d-grid">
                <input type="submit" class="btn btn-primary" value="Register">
            </div>
        </form>
        <div class="footer">
            <a href="https://github.com/crowdproj/opentutor" target="_blank">https://github.com/crowdproj/opentutor</a>
            &bull; &copy; 2024 sszuev
        </div>
    </div>
</div>
<script src="${url.resourcesPath}/js/bootstrap.bundle.min.js"></script>
<script>
    document.getElementById('toggle-password').addEventListener('click', function (e) {
        const passwordInput = document.getElementById('password');
        const icon = this.querySelector('i');
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            passwordInput.type = 'password';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    });
    document.getElementById('toggle-password-confirm').addEventListener('click', function (e) {
        const passwordInput = document.getElementById('password-confirm');
        const icon = this.querySelector('i');
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            passwordInput.type = 'password';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    });
</script>
</body>
</html>