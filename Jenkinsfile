
def mapBranchToEnvironment(branch) {
  if (branch == 'master') {
    return '[PRODUCTION]'
  }
  if (branch == 'develop') {
    return '[TEST]'
  }
  return '[DEVELOPMENT]'
}

def mapBranchToAppName(branch) {
  def appName = 'freyraum-mail-service'
  if (branch == 'master') {
    return appName
  }
  if (branch == 'develop') {
    return appName + '-int'
  }
  return appName + '-tst'
}

def mapBranchToDockerImage(branch) {
  def appName = 'freyraum-mail-service'
  if (branch == 'master') {
    return appName + ':latest'
  }
  if (branch == 'develop') {
    return appName + ':next'
  }
  return appName + ':snapshot'
}

pipeline {
  agent none
  options {
    skipDefaultCheckout()
  }
  environment{
    ENV_NAME = mapBranchToEnvironment("${BRANCH_NAME}")
    APP_NAME = mapBranchToAppName("${BRANCH_NAME}")
    DOCKER_IMAGE = mapBranchToDockerImage("${BRANCH_NAME}")
    BRANCH = "${BRANCH_NAME}"
  }
  stages {
    stage('checkout') {
      agent any
      steps {
        checkout scm
      }
    }

    stage('build application') {
      agent {
        docker { image 'openjdk:8-jdk-alpine' }
      }
      steps {
        sh './gradlew clean build'
      }
    }

    stage('test application') {
      agent {
        docker { image 'openjdk:8-jdk-alpine' }
      }
      steps {
        sh './gradlew test'
      }
    }

    stage('build jar') {
      agent {
        docker { image 'openjdk:8-jdk-alpine' }
      }
      steps {
        sh './gradlew bootJar'
      }
    }

    stage('containerize') {
      agent any
      steps {
        sh 'docker build . -f Dockerfile -t ${APP_NAME}'
        sh 'docker tag ${APP_NAME} localhost:5000/${DOCKER_IMAGE}'
        sh 'docker push localhost:5000/${DOCKER_IMAGE}'
      }
    }

  }

  post {
    success {
      slackSend(color: "#BDFFC3", message: "${ENV_NAME} created new docker image successfully: - ${DOCKER_IMAGE}")
    }
    failure {
      slackSend(color: "#FF9FA1", message: "${ENV_NAME} build failed - ${env.BRANCH} ${env.BUILD_NUMBER}")
    }
  }
}
