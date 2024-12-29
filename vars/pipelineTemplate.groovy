import org.practicaldevops.*

def call(serviceName) {
    // Constants and default values
    def AWS_REGION = 'ap-southeast-1'
    def ECR_REPO = 'practical-devops-ecr'
    def AWS_ACCOUNTID = '307946653621'
    def NAMESPACE = 'practical-devops-ns'
    def DEPLOYMENT_NAME ='development'
    def CLUSTER_NAME = 'practical-devops-eks'
    def ECR_REGISTRY = "${AWS_ACCOUNTID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
    def IMAGE_TAG= "SD1096-${serviceName}.${BUILD_NUMBER}-${new Date().format('yyyyMMddHHmmss')}"
    def CONTAINER_NAME="${serviceName}-container"
    def global = new Global()

    // Step 1: Login ECR
    global.loginToECR(AWS_REGION: AWS_REGION, ECR_REGISTRY: ECR_REGISTRY)

    // Step 2: Build docker images with the new tag
    global.buildDockerImages(ECR_REPOSITORY: ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)

    // Step 3: Tag
    global.tagDockerImages(ECR_REPOSITORY: ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)

    // Step 4: Push image to image registry
    global.pushDockerImages(ECR_REPOSITORY:ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG)

    // Step 5: Deploy image to EKS
    global.deployToEKS(CLUSTER_NAME:CLUSTER_NAME, NAMESPACE:NAMESPACE, DEPLOYMENT_NAME:DEPLOYMENT_NAME, ECR_REPOSITORY:ECR_REPOSITORY, IMAGE_TAG:IMAGE_TAG, CONTAINER_NAME:CONTAINER_NAME)
}
