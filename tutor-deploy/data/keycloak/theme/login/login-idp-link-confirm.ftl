<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Account Already Exists - opentutor</title>
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
            <h1>Account Already Exists</h1>
        </div>
        <div class="alert alert-danger">
            <#if userEmail??>
                User with email ${userEmail} already exists. How do you want to continue?
            <#else>
                An account with the provided details already exists. How do you want to continue?
            </#if>
        </div>
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary" name="submitAction" value="updateProfile">Review Profile
                </button>
                <button type="submit" class="btn btn-secondary" name="submitAction" value="linkAccount">Add to Existing
                    Account
                </button>
            </div>
        </form>
        <div class="footer">
            <a href="${url.loginUrl}">« Back to Login</a>
        </div>
    </div>
</div>
</body>
</html>