# Keycloak GDPR Event Logger

This extension serves as a replacement for the 
[default](https://www.keycloak.org/docs/latest/server_admin/#event-listener) 
`jboss-logging` event logger. By default, the logger 
includes several identifying attributes (like username 
or ip address) that should not be logged, if you want 
to be GDPR compliant.

The event logger in this extension behaves exactly 
like the `jboss-logging` logger except that these 
attributes are masked if necessary. Assuming for 
example the username is also the email in your realm. 
By default, the full email would show up in the event 
log. 

## Changes

This extension would mask the username like in the following example:

**Original username**
```
johndoe@test.com
```

**Masked username**
```
joh****@test.com
```

As you can see, it is no longer possible to identity 
the user with this information, but leaves a bit of 
room for debugging, if the same user occurs in 
multiple logs.

Currently, the following masking rules are implemented for the user:

* mask the username in event logs
  * if the username does not contain an @ symbol mask the entire username
  * if the username looks like an email address with local and domain part: mask the local part after the third character
  * if the local part is shorter than 3 characters: mask the complete local part
  * if the username contains an @ symbol, but parsing fails: mask the username completly
* remove the ip address from logs

## Installation

* Download the latest release from the release page
* Move the jar to `/opt/keycloak/providers`
* Make sure it is loaded correctly by checking the provider info in the admin-console
* Go to your realm > Realm Settings > Events
* Add the event listener `gdpr-jboss-logging`

## Configuration

The logger can be configured with the same configuration that is used for the jboss logger expect for the different provider id:

```shell
--spi-events-listener-gdpr-jboss-logging-success-level=info
--spi-events-listener-gdpr-jboss-logging-error-level=error
```

## Example Output

See below for some example logs (some values have been shortend for readability):
```shell
2024-04-28 11:55:47 keycloak-1  | 2024-04-28 09:55:47,397 WARN  [org.keycloak.events] (executor-thread-4) type="LOGIN", realmId="e4...76", clientId="security-admin-console", userId="5c3d4742-0941-4cfd-9d3f-163123d9184f", auth_method="***", auth_type="code", response_type="code", redirect_uri="http://loc...84f/settings", consent="no_consent_required", code_id="68...65", username="tes*********@test.com", response_mode="fragment", authSessionParentId="68...65", authSessionTabId="hIoKeXkcMrY",
2024-04-28 11:55:47 keycloak-1  | 2024-04-28 09:55:47,530 WARN  [org.keycloak.events] (executor-thread-5) type="CODE_TO_TOKEN", realmId="e4...76", clientId="security-admin-console", userId="5c3d4742-0941-4cfd-9d3f-163123d9184f", token_id="c70...23", grant_type="authorization_code", refresh_token_type="Refresh", scope="openid email profile", refresh_token_id="b31...1d8", code_id="68...65", client_auth_method="client-secret"
```
