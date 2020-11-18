pipelineJob ('Pipeline') {
    definition {
        cps {
            script('''def secrets = [
        [path: 'kv/secret', engineVersion: 2, secretValues: [
            [envVar: 'key', vaultKey: 'key']
        ]]
    ]

pipeline {
  agent any
  stages {
      stage ('Stage') {
          steps {
              withVault(vaultSecrets: secrets) {
                sh 'echo $key > secret.txt'
              }
          }
      }
  }
}'''
            )
        }
    }
}

job ('Freestyle') {
    wrappers {
        withVault {
            vaultSecrets {
                vaultSecret {
                    path("kv/secret")
                    secretValues {
                        vaultSecretValue {
                            envVar("key")
                            vaultKey("key")
                        }
                    }
                }
            }
        }
    }
    steps {
        shell('echo $key')
    }
}