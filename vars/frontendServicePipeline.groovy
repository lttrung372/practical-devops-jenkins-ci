#!/usr/bin/env groovy
import org.practicaldevops.*

void call(Map pipelineParams) {
    def AWS_REGION = 'ap-southeast-1'
    def ECR_REPO = 'practical-devops-ecr'
    def AWS_ACCOUNTID = '307946653621'
    def serviceName = 'frontend'
    def ECR_REGISTRY = "${AWS_ACCOUNTID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    def IMAGE_TAG = "SD1096-${serviceName}.${BUILD_NUMBER}-${new Date().format('yyyyMMddHHmmss')}"
    def CONTAINER_NAME = "${serviceName}-container"
    def CLUSTER_NAME = "practical-devops-eks"
    def NAMESPACE = "${serviceName}-ns"
    def DEPLOYMENT_NAME = "${serviceName}-development"
    def ECR_REPOSITORY = "${ECR_REPO}"

    def global = new Global()

    pipeline {
        agent any

        triggers {
            githubPush() // Ensure this trigger is properly defined in Jenkins
        }

        options {
            disableConcurrentBuilds()
            disableResume()
            timeout(time: 1, unit: 'HOURS')
        }

        stages {
            stage('Login to AWS ECR') {
                steps {
                    script {
                        withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
                            sh """
                                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            """
                        }
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        dir('src/frontend') {
                            sh """
                                docker build -t ${ECR_REPO}:${IMAGE_TAG} .
                            """
                        }
                    }
                }
            }

            stage('Tag Docker Image') {
                steps {
                    script {
                        sh """
                            docker tag ${ECR_REPO}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
                        """
                    }
                }
            }

            stage('Push to ECR') {
                steps {
                    script {
                        sh """
                            docker push ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
                        """
                    }
                }
            }

            stage('Deploy to EKS') {
                steps {
                    script {
                        // Ensure that you are using the correct AWS credentials and region
                        withAWS(credentials: 'AWSCredentails', region: AWS_REGION) {
                            // Ensure the deployToEKS function is available in the global context
                            global.deployToEKS(
                                CLUSTER_NAME: CLUSTER_NAME,
                                NAMESPACE: NAMESPACE,
                                DEPLOYMENT_NAME: DEPLOYMENT_NAME,
                                ECR_REPOSITORY: ECR_REPOSITORY,
                                IMAGE_TAG: IMAGE_TAG,
                                CONTAINER_NAME: CONTAINER_NAME
                )
                        }
                    }
                }
            }
        }

        post {
            always {
                echo 'Pipeline completed.'
            }
            cleanup {
                cleanWs()
            }
        }
    }
}
