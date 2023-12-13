<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes">
  <title><#if model.serviceName??>${model.serviceName} | </#if>Authorization Page</title>
  <link rel="stylesheet" href="/css/authorization.css">
</head>
<body class="font-default">
  <#if model.serviceName??>
  <div id="page_title">${model.serviceName}</div>
  </#if>

  <div id="content">
    <#if model.clientName??>
    <h3 id="client-name">${model.clientName}</h3>
    </#if>
    <div class="indent">
      <img id="logo" src="${model.logoUri!}" alt="[Logo] (150x150)">

      <div id="client-summary">
        <p>${model.description!}</p>
        <ul id="client-link-list">
          <#if model.clientUri??>
          <li><a target="_blank" href="${model.clientUri}">Homepage</a>
          </#if>

          <#if model.policyUri??>
          <li><a target="_blank" href="${model.policyUri}">Policy</a>
          </#if>

          <#if model.tosUri??>
          <li><a target="_blank" href="${model.tosUri}">Terms of Service</a>
          </#if>
        </ul>
      </div>

      <div style="clear: both;"></div>
    </div>

    <#if model.scopes??>
    <h4 id="permissions">Permissions</h4>
    <div class="indent">
      <p>The application is requesting the following permissions.</p>

      <dl id="scope-list">
        <#list model.scopes as scope>
        <dt>${scope.name!}</dt>
        <dd>${scope.description!}</dd>
        </#list>
      </dl>
    </div>
    </#if>

    <h4 id="authorization">Authorization</h4>
    <div class="indent">
      <p>Do you grant authorization to the application?</p>

      <form id="authorization-form" action="/api/authorization/decision" method="POST">
        <#if model.user??>
        <div id="login-user"><i>Logged in as ${model.user.subject!}</i></div>
        <#else>
        <div id="login-fields" class="indent">
          <div id="login-prompt">Input Login ID and password.</div>
          <input type="text" id="loginId" name="loginId" placeholder="Login ID"
                 class="font-default" required value="${model.loginId!}" ${model.loginIdReadOnly!}>
          <input type="password" id="password" name="password" placeholder="Password"
                 class="font-default" required>
        </div>
        </#if>
        <div id="authorization-form-buttons">
          <input type="submit" name="authorized" id="authorize-button" value="Authorize" class="font-default"/>
          <input type="submit" name="denied"     id="deny-button"      value="Deny"      class="font-default"/>
        </div>
      </form>
    </div>
  </div>

</body>
</html>
