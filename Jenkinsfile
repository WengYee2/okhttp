pipeline {
  agent any

  environment {
    DOCKER_CREDS = credentials('docker-hub-creds')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build and Test') {
      steps {
        bat 'gradlew clean build -Dtest.java.version=21 -Dokhttp.platform=jdk9'
      }
    }

    stage('Build Docker Image') {
      steps {
        bat 'docker build -t %DOCKER_CREDS_USR%/okhttp-custom:latest .'
      }
    }

    stage('Docker Login') {
      steps {
        bat 'docker login -u %DOCKER_CREDS_USR% -p %DOCKER_CREDS_PSW%'
      }
    }

    stage('Push Docker Image') {
      steps {
        bat 'docker push %DOCKER_CREDS_USR%/okhttp-custom:latest'
      }
    }
  }
}
