# Installation

The installation migth seem a little complex, but is pretty straight forward

## Create user accounts

  * ssh into your server
  * create a user jenkins
    * `useradd jenkins`
  * create a user agent
    * `useradd agent`
  * create an ssh-key for jenkins 
    * `su - jenkins`
    * `ssh-keygen -t ed25519`
    * `exit`
  * put jenkins public key into agents authorized keys
    * `su - agent`
    * `mkdir ~/.ssh && chmod 700 ~/.ssh` 
    * `touch ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && echo "<publicKey>" >> ~/.ssh/authorized_keys`
    * `exit`
  * install docker_rootless for jenkins (you need to ssh instead of `su`)
    * `curl -fsSL https://get.docker.com/rootless | sh | grep export >> ~/.bashrc` &&
    * `source ~/.bashrc`
    * `systemctl --user enable docker`
    * `loginctl enable-linger jenkins`
    * `systemctl --user restart docker`
  * install docker_rootless for agent (you need to ssh instead of `su`)
    * `curl -fsSL https://get.docker.com/rootless | sh | grep export >> ~/.bashrc` &&
    * `source ~/.bashrc`
    * `systemctl --user enable docker`
    * `loginctl enable-linger agent`
    * `systemctl --user restart docker`

## Configure firewall

Docker rootless in unable to configure your firewall automatically, as it does not run as root. You need to do this by yourself with `ufw allow proto tcp from any to any port 443`. This allows all tcp traffic on port 443 (so basically https).

## Setup traefik

  * replace *{{user-id}}* in docker-compose.prod.yaml with jenkins userid
    * `sed -i "s/{{user-id}}/$(id -u jenkins)/g" docker-compose.prod.yaml`
  * replace *{{fqdn}}* in docker-compose.prod.yaml with the FQDN of this server
    * `sed -i "s/{{fqdn}}/< FQDN >/g" docker-compose.prod.yaml`
  
## Setup vault

  * start traefik and vault
    * `docker-compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d vault traefik`
  * create an alias for vault
    * `alias vault='docker exec -it vault vault`
  * initialize vault
    * `vault operator init`
  * copy all unseal codes and the root token to a secure place, you will need this
  * unseal vault
    * `vault operator unseal <unsealcode1>`
    * `vault operator unseal <unsealcode2>`
    * `vault operator unseal <unsealcode3>`
  * enable authentication methods
    * `vault login` with your root token
    * `vault auth enable ldap`
    * `vault auth enable approle`
    * `vault auth tune -default-lease-ttl=8h ldap`
  * open vault in your web browser and configure the ldap connector

## Jenkins

  * add the public key of the jenkins linux user to your git repository
  * update *{{git-remote-url}}* in jenkins/seed.groovy
    * `sed -i "s/{{git-remote-url}}/< git remote url >/g" jenkins/seed.groovy`
  * start jenkins
    * `docker-compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d --build`
  * update the credential that will be used to connect to a git repository
    * open https://your.jenkins/credentials/store/system/domain/_/credential/jenkins-git/update
  * update the credential that will be used to conenct to agents
    * open https://your.jenkins/credentials/store/system/domain/_/credential/jenkins-build-node/update
  * copy jenkins *secret.key* into *data/jenkins/secret.key*
    * `docker cp jenkins:secret.key data/jenkins/secret.key`
  * copy jenkins *secrets* directory to *data/jenkins/secrets*
    * `docker cp jenkins:secrets data/jenkins/secrets`
  * setup your ldap connection at https://your.jenkins/configureSecurity/
  * don't forget to set the permissions
  * set your technical user as the user that runs all builds
  * export all changed data from https://your.jenkins/configuration-as-code/viewExport to *jenkins.casc.yaml*
    * credentials
    * jenkins->authorizationStrategy->projectMatrix->permissions
    * jenkins->securityRealm (there is always a typo at ldap->configurations->inhibitferRootDN, should be inhibitinferRootDN)
    * security->queueItemAuthenticators
  * replace *{{fqdn}}* in jenkins/casc.yaml with the FQDN of this server
    * `sed -i "s/{{fqdn}}/< FQDN >/g" jenkins/casc.yaml`
  * replace *{{fqdn}}* in jenkins/casc_local.yaml with the FQDN of this server
    * `sed -i "s/{{fqdn}}/< FQDN >/g" jenkins/casc_local.yaml`

## Finalize

  * create a docker-compose .env file
    * `touch .env && echo -e "COMPOSE_FILE=docker-compose.yaml;docker-compose.prod.yaml;docker-compose.prod.override.yaml\nCOMPOSE_PATH_SEPARATOR=;" > .env`
  * Commit and push your changes and
  * final restart
    * `docker-compose up -d`