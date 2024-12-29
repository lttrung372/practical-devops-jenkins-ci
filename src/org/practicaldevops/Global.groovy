package org.practicaldevops

// def pythonRunInstallDependencies(){
//     stage ("Run Install Dependencies ") {
//         sh "mkdir -p results"
//         sh 'docker run --rm -v $(pwd):/app python:3.9-slim bash -c "pip install poetry && cd /app && poetry config virtualenvs.in-project true && poetry install"'
//     }
// }

def loginToECR(args) {
    def AWS_REGION = args.AWS_REGION
    def ECR_REGISTRY = args.ECR_REGISTRY

    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        stage('Login to AWS ECR') {
            script {
                sh """
                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                """
            }
        }
    }
}

def buildDockerImages(args) {
    def ECR_REPOSITORY = args.ECR_REPOSITORY
    def IMAGE_TAG = args.IMAGE_TAG
    def AWS_REGION = args.AWS_REGION

    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        stage('Build Docker Images') {
            script {
                sh """
                docker build -t ${ECR_REPOSITORY}:${IMAGE_TAG} .
                """
            }
        }
    }
}

def tagDockerImages(args) {
    def ECR_REPOSITORY = args.ECR_REPOSITORY
    def IMAGE_TAG = args.IMAGE_TAG
    def AWS_REGION = args.AWS_REGION

    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        stage('Tag Docker Images') {
            script {
                sh """
                docker tag ${ECR_REPOSITORY}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }
    }
}

def pushDockerImages(args) {
    def ECR_REPOSITORY = args.ECR_REPOSITORY
    def IMAGE_TAG = args.IMAGE_TAG
    def AWS_REGION = args.AWS_REGION

    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        stage('Push Docker Images') {
            script {
                sh """
                docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }
    }
}

def deployToEKS(args) {
    def CLUSTER_NAME = args.CLUSTER_NAME
    def NAMESPACE = args.NAMESPACE
    def DEPLOYMENT_NAME = args.DEPLOYMENT_NAME
    def ECR_REPOSITORY = args.ECR_REPOSITORY
    def IMAGE_TAG = args.IMAGE_TAG
    def CONTAINER_NAME = args.CONTAINER_NAME
    def AWS_REGION = args.AWS_REGION

    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        stage('Push Docker Images') {
            script {
                sh """
            # Update kubeconfig
            aws eks update-kubeconfig --name ${CLUSTER_NAME}

            # Deploy to EKS
            kubectl set image deployment/${DEPLOYMENT_NAME} \
                ${CONTAINER_NAME}=${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG} \
                -n ${NAMESPACE}

            # Wait for rollout to complete
            kubectl rollout status deployment/${DEPLOYMENT_NAME} -n ${NAMESPACE}
        """
            }
        }
    }
}
