# How to add a new group?

Creating new groups requires configuration for both vault and jenkins.

We differ between the terminonlogies *teamname* and *ldap_group*. ldap_group is the technical name of the teams ldap group, while teamname is the more human readable name.

## Vault

We need to create a new policy for the team. Simply replace <teamname> with the teams folder name and put the result at *vault/groups/<ldap_group>.hcl*:

```
# List everything under <teamname>/
# View metadata for everything under <teamname>/ (enables history and delete and destroy for versions of secrets)
# Delete whole secret
path "<teamname>/metadata/*" {
    capabilities = [ "list", "read", "delete" ]
}

# Create a new secret
# Read a secret
# Create new version of a secret
# Delete latest version of a secret
path "<teamname>/data/*" {
    capabilities = [ "create", "read", "update", "delete" ]
}

# Restore a deleted version of a secret
path "<teamname>/undelete/*" {
  capabilities = [ "update" ]
}

# Delete a specific version of a secret
path "<teamname>/delete/*" {
  capabilities = [ "update" ]
}

# Permanently remove a version of a secret
path "<teamname>/destroy/*" {
  capabilities = [ "update" ]
}
```

Next, upload the policy with `(docker exec -it vault) vault policy write <ldap_group> /vault/config/groups/<ldap_group>.hcl`, and enable the policy for the ldap group by executing `(docker exec -it vault) vault vault write auth/ldap/groups/<ldap_group> policies=<ldap_group>`.

Now create an app-role by executing `(docker exec -it vault) vault write auth/approle/role/<ldap_group> token_policies=<ldap_group>`. </br>
You will get the role-id with `(docker exec -it vault) vault get auth/approle/role/<ldap_group>/role-id` and the secret-id `docker exec -it vault) vault write -f auth/approle/role/<ldap_group>/secret-id`. 

## Jenkins

We also need a configuration file, so put the following configuration at *jenkins/teams/<ldap_group>.groovy* and replace some placeholders:

``` groovy
def vaultDomain = "<teamname>-vault"
def vaultCredential = "<teamname>-vault"
def vaultAppRoleId = "<role-id from vault step>"
def vaultAppRoleSecret = "<secret-id from vault step>"

folder("<team-name>") {
  authorization {
    permission("hudson.model.Item.Build", "<ldap_group>")
    permission("hudson.model.Item.Read", "<ldap_group>")
    permission("hudson.model.Item.Workspace", "<ldap_group>")
  }
  properties {
    folderCredentialsProperty {
      domainCredentials {
        domainCredentials {
          domain {
            description("Vault")
            name(vaultDomain)
          }
        }
      }
    }

    folderVaultConfiguration {
      configuration {
        vaultCredentialId(vaultCredential)
      }
    }
  }

  configure { folderXml ->
    def domainCredentialsMap = folderXml /
        "properties" /
        "com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider_-FolderCredentialsProperty" /
        "domainCredentialsMap"

    def vaultEntry = domainCredentialsMap."*".find { node ->
      node."com.cloudbees.plugins.credentials.domains.Domain".name.text() == vaultDomain
    }
    def vaultList = vaultEntry / "java.util.concurrent.CopyOnWriteArrayList"
    vaultList << "com.datapipe.jenkins.vault.credentials.VaultAppRoleCredential" {
      id(vaultCredential)
      path("approle")
      description("<teamname> approle token for vault")
      roleId(vaultAppRoleId)
      scope("GLOBAL")
      secretId(vaultAppRoleSecret)
    }
  }
}

job("<teamname>/Seed") {
  scm {
    git {
      remote {
        url "<url of the teams job repository"
        credentials "jenkins-git"
      }
      extensions {
        cloneOptions {
          shallow = true
        }
      }
    }
  }
  steps {
    dsl {
      external "*.groovy"
      removeAction "DELETE"
      ignoreExisting
    }
  }
  properties {
    authorizeProjectProperty{
      strategy {
        triggeringUsersAuthorizationStrategy()
      }
    }
  }
}

queue("<teamname</Seed")
```

Now login as an administrator into jenkins and run the Seed pipeline in the Admin folder.