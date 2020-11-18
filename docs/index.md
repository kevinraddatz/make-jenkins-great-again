# Make Jenkins Great Again

## Some context

Our jenkins *historically grew* with every team that wanted to use it, which resulted in a very misconfigured jenkins, nobody had the courage to update plugins or jenkins itself as this might break something.

This project aims to provide a jenkins that serves those features:

  * users have pretty restricted access
  * externalized credentials manager with Hashicorp Vault
  * every configuration is trackable with a git repository
  * use a local instance of your jenkins to develop your pipelines
  * run builds on agents
  * low maintainance overhead
  * coming soon: easy installation

Currently this documentation is not separated into an admins and a developers guide, I am working on it.

This project currently uses one private key to access all repositories in a project. For security reasons you might need to inject the credentials of a technical user for each team into their teams folder in jenkins.