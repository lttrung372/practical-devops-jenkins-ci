import org.practicaldevops.*

def call(serviceName) {
    // Constants and default values
    def AWS_REGION = 'ap-southeast-1'
    def ECR_REPO = 'practical-devops-ecr'
    def AWS_ACCOUNTID = '307946653621'
    def NAMESPACE = 'practical-devops-ns'
    def DEPLOYMENT_NAME = 'development'
    def CLUSTER_NAME = 'practical-devops-eks'
    def ECR_REGISTRY = "${AWS_ACCOUNTID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
    def IMAGE_TAG = "SD1096-${serviceName}.${BUILD_NUMBER}-${new Date().format('yyyyMMddHHmmss')}"
    def CONTAINER_NAME = "${serviceName}-container"
    def global = new Global()

    // withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
    //     stage('Login to AWS ECR') {
    //         global.loginToECR(AWS_REGION: AWS_REGION, ECR_REGISTRY: ECR_REGISTRY)
    //     }

    //     stage('Build docker images') {
    //         global.buildDockerImages(ECR_REPOSITORY: ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)
    //     }

    //     stage('Tag docker images') {
    //         global.tagDockerImages(ECR_REPOSITORY: ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)
    //     }

    //     stage('Push docker images to ECR') {
    //         global.pushDockerImages(ECR_REPOSITORY:ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)
    //     }

    //     stage('Deploy to EKS') {
    //         global.deployToEKS(CLUSTER_NAME:CLUSTER_NAME, NAMESPACE:NAMESPACE, DEPLOYMENT_NAME:DEPLOYMENT_NAME, ECR_REPOSITORY:ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG, CONTAINER_NAME:CONTAINER_NAME)
    //     }
    // }
    withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
        
        stage('Login to AWS ECR') {
            script {
                sh """
                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                """
            }
        }

        stage('Build Docker Image') {
            script {
                sh """
                docker build -t ${ECR_REPOSITORY}:${IMAGE_TAG} .
                """
            }
        }

        stage('Tag Docker Image') {
            script {
                sh """
                docker tag ${ECR_REPOSITORY}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }

        stage('Push to ECR') {
            script {
                sh """
                docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }
    }
}
