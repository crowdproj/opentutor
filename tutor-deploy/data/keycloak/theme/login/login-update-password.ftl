<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Password - opentutor</title>
    <link rel="icon" href="${url.resourcesPath}/favicon.ico" type="image/x-icon">
    <link href="${url.resourcesPath}/css/bootstrap.min.css" rel="stylesheet">
    <link href="${url.resourcesPath}/css/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        .container {
            max-width: 500px;
            margin: 0 auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            margin-top: 50px;
        }

        .header {
            text-align: center;
            margin-bottom: 20px;
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
<div class="container">
    <div class="header">
        <h1>Update Password</h1>
        <p class="subtitle">You need to change your password</p>
    </div>
    <#if message?has_content>
        <div class="alert alert-danger" role="alert">
            ${message.summary!?no_esc}
        </div>
    </#if>
    <form id="kc-passwd-update-form" action="${url.loginAction}" method="post">
        <div class="mb-3">
            <label for="password-new" class="form-label">New Password</label>
            <div class="input-group">
                <input type="password" id="password-new" name="password-new" class="form-control" required
                       autocomplete="new-password">
                <button class="btn btn-outline-secondary" type="button" aria-label="Show password"
                        id="toggle-password-new">
                    <i class="bi bi-eye"></i>
                </button>
            </div>
        </div>
        <div class="mb-3">
            <label for="password-confirm" class="form-label">Confirm Password</label>
            <div class="input-group">
                <input type="password" id="password-confirm" name="password-confirm" class="form-control" required
                       autocomplete="new-password">
                <button class="btn btn-outline-secondary" type="button" aria-label="Show password"
                        id="toggle-password-confirm">
                    <i class="bi bi-eye"></i>
                </button>
            </div>
        </div>
        <div class="form-check mb-3">
            <input type="checkbox" class="form-check-input" id="logout-sessions" name="logout-sessions" value="on"
                   checked>
            <label class="form-check-label" for="logout-sessions">Sign out from other devices</label>
        </div>
        <div class="d-grid">
            <input type="submit" class="btn btn-primary" value="Submit">
        </div>
    </form>
    <div class="footer">
        <a href="https://github.com/crowdproj/opentutor" target="_blank">https://github.com/crowdproj/opentutor</a>
        &bull; &copy; 2024 sszuev
    </div>
</div>

<script>
    document.getElementById('toggle-password-new').addEventListener('click', function () {
        const passwordInput = document.getElementById('password-new');
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

    document.getElementById('toggle-password-confirm').addEventListener('click', function () {
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
