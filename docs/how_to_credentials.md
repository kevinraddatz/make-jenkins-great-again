# How to credentials?

Thanks to vault using credentials is pretty easy and only slightly differs between prod and local

## Prod

Every team uses their own vault approle (like a technical user) which has the same permissions as the team itself (simply means team and approle can do everything in their specific space). The admins seed job will make sure every team has a folder in jenkins, inject the team approles role-id and secret-id into the folder level credential named <teamname-vault> and configure the vault plugin on folder level to use those credentials.

The jobs now just have to specify which secrets they want to use [How to pipelines?](how_to_pipelines.md)

## Local

The administrator surely doesn't want to give you role-id and secret-id of your technical user, but there is a way to work around this: You can log into your vault at https://your.vault/ using the ldap authentication method and then copy your individual token at the top right of the page (this token will expire after 8 hours). You will find preparated credentials for vault at http://localhost:8080/credentials/store/system/domain/_/credential/vault/update, simply put your token here and you are good to go. The url of your vault should already be there, go to http://localhost:8080/configure and search for "Vault Plugin".

You can now go ahead and build your pipeline, both stages now use the same credentials