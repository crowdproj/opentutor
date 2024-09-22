<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Account Information - opentutor</title>
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
        <h1>Update Account Information</h1>
    </div>
    <#if message?has_content>
        <div class="alert alert-danger" role="alert">
            ${message.summary!?no_esc}
        </div>
    </#if>
    <form id="kc-update-profile-form" class="form-horizontal" action="${url.loginAction}" method="post">
        <div class="mb-3">
            <label for="firstName" class="form-label">First Name</label>
            <input type="text" id="firstName" name="firstName" class="form-control" value="${user.firstName!}" required>
        </div>
        <div class="mb-3">
            <label for="lastName" class="form-label">Last Name</label>
            <input type="text" id="lastName" name="lastName" class="form-control" value="${user.lastName!}" required>
        </div>
        <div class="mb-3">
            <label for="email" class="form-label">Email</label>
            <input type="email" id="email" name="email" class="form-control" value="${user.email!}" required>
        </div>
        <div class="d-grid">
            <input type="submit" class="btn btn-primary" value="Update Information">
        </div>
    </form>
    <div class="footer">
        <a href="${url.loginUrl}">« Back to Login</a>
    </div>
</div>
</body>
</html>
