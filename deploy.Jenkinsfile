pipeline {
  agent any
  options {
    skipDefaultCheckout()
    timeout(time: 5, unit: 'MINUTES')
  }
  parameters {
    choice(name: 'TAG', choices: ['ok', 'rc'], description: 'docker image tag')
    string(name: 'APP_NAME', defaultValue: 'freyraum-mail-service', description: 'container name')
    string(name: 'APP_PORT', defaultValue: '7700', description: 'container port')
  }
  environment {
    DOCKER_REGISTRY = "localhost:5000"
    SPRING_PROFILES_ACTIVE = "prod"
    MAIL_HOST = "smtp.1und1.de"
    MAIL_PORT = "587"
    MAIL = credentials('mail')
  }
  stages {
    stage('pull image') {
      input {
        message "Confirm update"
        ok "update container"
      }
      steps { sh 'docker pull ${DOCKER_REGISTRY}/${APP_NAME}:${TAG}' }
    }
    stage('stop app') {
      steps { sh 'docker stop ${APP_NAME} || true' }
    }
    stage('remove app') {
      steps { sh 'docker rm ${APP_NAME} || true' }
    }
    stage('run app') {
      steps {
        sh '''
          docker run -d \
            -p ${APP_PORT}:7700 \
            --restart=always \
            --name ${APP_NAME} \
            -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
            -e MAIL_HOST=${MAIL_HOST} \
            -e MAIL_PORT=${MAIL_PORT} \
            -e MAIL_PASSWORD=${MAIL_PSW} \
            -e MAIL_USER=${MAIL_USR} \
            ${DOCKER_REGISTRY}/${APP_NAME}:${TAG}
        '''
      }
    }

  }
  post {
    success {
      slackSend(color: "#BDFFC3", message: "${APP_NAME}:${TAG} started")
    }
    failure {
      slackSend(color: "#FF9FA1", message: "${APP_NAME}:${TAG} - failed to update - app down!")
    }
  }

}
