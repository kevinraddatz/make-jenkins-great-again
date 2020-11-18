# How to pipelines?

Building pipelines is pretty easy.

TL/DR: Just create a groovy file with everything configured as you would do in the UI.

If you need help with your configuration, have a look at https://your.jenkins/plugin/job-dsl/api-viewer/index.html#path/pipelineJob-definition-cpsScm-scm-git-branch. The pages needs some time to load, but then presents you everything that you need to build your pipeline.

## Development

You might agree that it is not a good idea to abuse your production stage for development, but fortunately this project gives you everything to develop your jobs securely on a local instance. As every sensitive information is properly encrypted, the administrator can use a public repository to host the configuration. Now, as a developer, simply clone this repository and run `docker-compose up -d`. This will spin up a local jenkins fully independent of the production one. 

In the root folder of this project you will find a directory *jobs*. When developing a job, simply copy your jobs groovy file into this directory and use the [local job](http://localhost:8080/job/LocalSeed/) to import all changes. </br>**Keep in mind:** On production you most propably will have your jobs in a separate folder, so make sure to update the folder.groovy in the *jobs* directory.

There is already a credential prepopulated that can be used for accessing a git repository. Open http://localhost:8080/credentials/store/system/domain/_/credential/jenkins-git/update and put your privateKey and passphrase here. If your production jenkins uses a different credential than *jenkins-git* make sure to update the id of the local credential.

### FreeStyle

A freestyle job using some [vault credentials](how_to_credentials.md) might look like this:

``` groovy
job('FreeStyle') {
  wrappers {
    buildInDokcer {
      image('ubuntu')
    }
    withVault {
      vaultSecrets {
        vaultSecret {
          path('team/secret')
          secretValues {
            vaultSecretValue {
              envVar('env')
              vaultKey('key')
            }
          }
        }
      }
    }
  }
  steps {
    shell('echo $env')
  }
}
```

This freestyle job runs inside an ubuntu container, gets a credential from vault injected and finally prints this credential on the shell.

### Pipeline

The job from above looks a little different when using a pipeline:

``` groovy
pipelineJob('Pipeline') {
  definition {
    cps {
      script {'
pipeline {
  agent {
    docker {
      image "ubuntu"
    }
  }
  stages {
    stage ("Print secret") {
      withVault([vaultSecrets: [
        [path: "team/secret", secretValues: [
          [envVar: "key", vaultKey: "key"]
        ]]
      ]]) {
        sh "echo $key"
      }
    }
  }
}'
      }
    }
  }
}
```

Here everything is defined inside pipeline script (Jenkinsfile). You can also use git to check out a Jenkinsfile:

``` groovy
pipelineJob('Pipeline') {
  definition {
    cpsScm {
      scm {
        git {
          remote {
            url('https://your.git/repository.git')
            credentials('git-credentials')
          }
        }
      }
      scriptPath('Jenkinsfile')
    }
  }
}
```

### Multibranch Pipeline

A multibranch pipeline looks similar to a regular pipeline:

``` groovy
multibranchPipelineJob('Multibranch') {
  branchSources {
    branchSource {
      source {
        git {
          id('Multibranch')
          remote('https://your.git/repository.git')
          credentialsId('jenkisn-git')
          traits {
            gitTagDiscovery()
            gitBranchDiscovery()
          }
        }
      }
    }
  }
}
```