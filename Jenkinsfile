pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        DOCKER_IMAGE_NAME             = "sisyphus-backend"
        DOCKER_REGISTRY_URL           = "${env.DOCKER_REGISTRY_URL}"
        DEPLOY_TARGET_HOST            = "${env.DEPLOY_TARGET_HOST}"
        DEPLOY_USER                   = "${env.DEPLOY_USER}"
        SISYPHUS_FRONTEND_HOSTNAME    = "${env.SISYPHUS_FRONTEND_HOSTNAME}"
        OPENAI_API_KEY                = "${env.OPENAI_API_KEY}"

        DEPLOY_TARGET_DIR             = "/home/${DEPLOY_USER}/${DOCKER_IMAGE_NAME}"
        TAG_NAME                      = ""
    }

    stages {
        stage('Initialize') {
            steps {
                cleanWs()
                sh "docker-compose -p ${DOCKER_IMAGE_NAME} down || true"
                sh "docker system prune -af || true"
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'make test'
            }
        }

        stage('Build prod image') {
            steps {
                sh 'make prod'
            }
        }

        stage('Tag & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo """Running deploy for:
                        DOCKER_IMAGE_NAME: ${DOCKER_IMAGE_NAME}
                        DOCKER_REGISTRY_URL: ${DOCKER_REGISTRY_URL}
                        DEPLOY_TARGET_HOST: ${DEPLOY_TARGET_HOST}
                        DEPLOY_USER: ${DEPLOY_USER}
                        SISYPHUS_FRONTEND_HOSTNAME: ${SISYPHUS_FRONTEND_HOSTNAME}
                        OPENAI_API_KEY: ${OPENAI_API_KEY}
                    """

                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    TAG_NAME = "${DOCKER_IMAGE_NAME}:${commitHash}"

                    echo "Creating and pushing tag: ${TAG_NAME}"

                    sh "docker tag ${DOCKER_IMAGE_NAME} ${DOCKER_REGISTRY_URL}/${TAG_NAME}"
                    sh "docker push ${DOCKER_REGISTRY_URL}/${TAG_NAME}"

                    echo "Successfully pushed ${TAG_NAME} to Docker registry"
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                script {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_TARGET_HOST} \\
                            "mkdir -p ${DEPLOY_TARGET_DIR}"

                        scp -o StrictHostKeyChecking=no \
                            docker-compose.prod.yml \
                            ${DEPLOY_USER}@${DEPLOY_TARGET_HOST}:${DEPLOY_TARGET_DIR}/docker-compose.yml

                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_TARGET_HOST} \\
                            "cd ${DEPLOY_TARGET_DIR} \
                                && export DOCKER_REGISTRY_URL=${DOCKER_REGISTRY_URL} \
                                && export TAG_NAME=${TAG_NAME} \
                                && export SISYPHUS_FRONTEND_HOSTNAME=${SISYPHUS_FRONTEND_HOSTNAME} \
                                && export OPENAI_API_KEY=${OPENAI_API_KEY} \
                                && docker-compose pull \
                                && docker-compose down && docker image prune -f && docker-compose up -d
                            "
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh "docker-compose -p ${DOCKER_IMAGE_NAME} down || true"
            sh "docker system prune -af || true"
        }
    }
}
