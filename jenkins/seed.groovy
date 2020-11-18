folder('Admin') {}

job('Admin/TeamsSeed') {
  scm {
    git {
      remote {
        url '{{git-remote-url}}'
        credentials 'jenkins-git'
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
      external 'jenkins/teams/**/*.groovy'
      removeAction 'DELETE'
      ignoreExisting
    }
  }
}

queue('Admin/TeamsSeed')