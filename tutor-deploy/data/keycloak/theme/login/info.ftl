<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Information - opentutor</title>
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
        <h1>Information</h1>
    </div>
    <div class="alert alert-info" role="alert">
        <p>${message.summary}</p>
        <#if requiredActions??>
            <p><b>${msg("requiredAction.${requiredActions[0]}")}</b></p>
        </#if>
    </div>

    <#if pageRedirectUri?has_content>
        <div class="text-center mt-4">
            <a href="${pageRedirectUri}" class="btn btn-primary" role="button">
                ${msg("backToApplication")}
            </a>
        </div>
    <#elseif actionUri?has_content>
        <div class="text-center mt-4">
            <a href="${actionUri}" class="btn btn-primary" role="button">
                ${msg("proceedWithAction")}
            </a>
        </div>
    <#else>
        <div class="text-center mt-4">
            <a href="${properties.logoUrl}" class="btn btn-primary" role="button">
                ${msg("backToApplication")}
            </a>
        </div>
    </#if>

    <div class="footer">
        <a href="https://github.com/crowdproj/opentutor" target="_blank">https://github.com/crowdproj/opentutor</a>
        &bull; &copy; 2024 sszuev
    </div>
</div>
</body>
</html>
