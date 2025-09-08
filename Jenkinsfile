pipeline {
  agent any

  environment {
    DOCKER_CREDS = credentials('docker-hub-creds')
  }

  stages {
  stage('Debug') {
  steps {
    bat 'echo %DOCKER_USER%'
    bat 'docker --version'
    bat 'gradlew.bat --version'
  }
}
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
        steps {
            bat '.\\gradlew clean build -x test -Dtest.java.version=21 -Dokhttp.platform=jdk9'
        }
    }

    stage('Test') {
      steps {
        bat 'gradlew test -Dtest.java.version=21 -Dokhttp.platform=jdk9'
      }
    }
    stage('Deploy with Docker') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-hub-creds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          // Login to Docker Hub
          bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
          
          // Build Docker image with Dockerfile in the project root
          bat 'docker build -t %DOCKER_USER%/okhttp-custom:latest .'
          
          // Push Docker image to Docker Hub
          bat 'docker push %DOCKER_USER%/okhttp-custom:latest'
          
          // Optional: logout to clean up credentials
          bat 'docker logout'
        }
      }
    }
  }
  post {
      always {
        jacoco(
          execPattern: '**/build/jacoco/test.exec',
          classPattern: '**/build/classes/java/main',
          sourcePattern: '**/src/main/java',
          inclusionPattern: '**/*.class',
          exclusionPattern: '**/test/**'
      )
    }
    success {
      echo 'Pipeline completed successfully!'
    }
    failure {
      echo 'Pipeline failed. Please check the logs.'
    }
  }
}