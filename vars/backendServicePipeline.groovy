#!/usr/bin/env groovy
void call(Map pipelineParams) {
    def AWS_REGION = 'ap-southeast-1'
    def ECR_REPO = 'practical-devops-ecr'
    def AWS_ACCOUNTID = '307946653621'
    def serviceName = "backend"
    def ECR_REGISTRY = "${AWS_ACCOUNTID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
    def IMAGE_TAG = "SD1096-${serviceName}.${BUILD_NUMBER}-${new Date().format('yyyyMMddHHmmss')}"
    
    pipeline {
        agent any

        triggers {
            githubPush()
        }

        options {
            disableConcurrentBuilds()
            disableResume()
            timeout(time: 1, unit: 'HOURS')
        }

        stages {
            stage('Load Pipeline') {
                steps {
                    script {
                        dir('src/backend') {
                            //pipelineTemplate("backend")
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
                    }
                }
            }
        }

        post {
            cleanup {
                cleanWs()
            }
        }
    }
}
