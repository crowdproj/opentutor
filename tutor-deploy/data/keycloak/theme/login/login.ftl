<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in to opentutor</title>
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

        .login-header img {
            max-width: 200px;
            height: auto;
            margin-bottom: 10px;
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
            <img src="${url.resourcesPath}/img/logo.png" alt="Logo">
            <h1>Sign in to opentutor</h1>
        </div>
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
              method="post">
            <div class="mb-3">
                <label for="username" class="form-label">Email</label>
                <input tabindex="2" id="username" class="form-control" name="username" type="text" autofocus
                       autocomplete="username" aria-invalid="false">
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <input tabindex="3" id="password" class="form-control" name="password" type="password"
                           autocomplete="current-password" aria-invalid="false">
                    <button class="btn btn-outline-secondary" type="button" aria-label="Show password"
                            aria-controls="password" id="toggle-password">
                        <i class="bi bi-eye" aria-hidden="true"></i>
                    </button>
                </div>
            </div>
            <div class="mb-3 form-check">
                <input tabindex="5" id="rememberMe" name="rememberMe" type="checkbox" class="form-check-input">
                <label class="form-check-label" for="rememberMe">Remember me</label>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <a tabindex="6" href="${url.loginResetCredentialsUrl}">Forgot Password?</a>
            </div>
            <div class="d-grid">
                <input type="hidden" id="id-hidden-input" name="credentialId">
                <input tabindex="7" class="btn btn-primary" name="login" id="kc-login" type="submit" value="Sign In">
            </div>
        </form>
        <div class="text-center mt-3">
            <hr>
            <h2>Or sign in with</h2>
            <#if social.providers?? && social.providers?size gt 0>
                <#list social.providers as p>
                    <a id="social-${p.alias}" class="btn btn-outline-secondary w-100 my-2" type="button"
                       href="${p.loginUrl}">
                        <i class="bi bi-google"></i> ${p.displayName}
                    </a>
                </#list>
            </#if>
            <div id="kc-registration-container">
                <span>New user? <a tabindex="8" href="${url.registrationUrl}">Register</a></span>
            </div>
        </div>
    </div>
</div>
<div class="footer">
    <a href="https://github.com/crowdproj/opentutor" target="_blank">https://github.com/crowdproj/opentutor</a> &bull;
    &copy; 2024 sszuev
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
</script>
</body>
</html>