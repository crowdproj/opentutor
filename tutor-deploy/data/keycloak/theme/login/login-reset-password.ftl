<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Your Password? - opentutor</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" type="image/x-icon">
    <link href="${url.resourcesPath}/css/bootstrap.min.css" rel="stylesheet">
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
            <h1>Forgot Your Password?</h1>
        </div>
        <form id="kc-reset-password-form" action="${url.loginAction}" method="post">
            <div class="mb-3">
                <label for="username" class="form-label">Email</label>
                <input type="text" id="username" name="username" class="form-control" autofocus autocomplete="email">
            </div>
            <div class="d-grid">
                <button type="submit" class="btn btn-primary">Submit</button>
            </div>
            <div class="text-center mt-3">
                <a href="${url.loginUrl}">« Back to Login</a>
            </div>
        </form>
        <div class="footer">
            Enter your email and we will send you instructions on how to reset your password.
        </div>
    </div>
</div>
</body>
</html>