
pipeline {
  agent any
  options {
    skipDefaultCheckout()
  }
  environment{
    DOCKER_REGISTRY = "localhost:5000"
    APP_NAME = "freyraum-mail-service"
    RC_TAG = "rc"
    OK_TAG = "ok"
  }
  stages {
    stage('checkout') {
      steps { checkout scm }
    }

    stage('build application') {
      steps { sh './gradlew clean build -x test' }
    }

    stage('tests') {
      steps { sh './gradlew test' }
    }

    stage('build release candidate') {
      steps {
        sh './gradlew bootJar'
        sh 'docker tag ${APP_NAME} ${DOCKER_REGISTRY}/${APP_NAME}:${RC_TAG}'
        sh 'docker push ${DOCKER_REGISTRY}/${APP_NAME}:${RC_TAG}'
      }
    }

    stage('tag image as ok') {
      steps {
        sh 'docker tag ${DOCKER_REGISTRY}/${APP_NAME}:${RC_TAG} ${DOCKER_REGISTRY}/${APP_NAME}:${OK_TAG}'
        sh 'docker push ${DOCKER_REGISTRY}/${APP_NAME}:${OK_TAG}'
      }
    }
  }

  post {
    success {
      slackSend(
        color: "#BDFFC3",
        message: "${APP_NAME} - tagged container as ${OK_TAG}"
      )
    }
    failure {
      slackSend(
        color: "#FF9FA1",
        message: "${APP_NAME} build failed - ${env.BRANCH} ${env.BUILD_NUMBER}"
      )
    }
  }
}
