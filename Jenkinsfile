#!groovy

node {
    stage 'Checkout'
    checkout scm
    sh 'git clean -dfx'
    sh 'git rev-parse --short HEAD > git-commit'
    sh 'set +e && (git describe --exact-match HEAD || true) > git-tag'

    stage 'Build'
    sh "${tool 'm3'}/bin/mvn clean package dependency:copy-dependencies"

    stage 'Image'
    def branch   = env.JOB_NAME.replaceFirst('.+/', '')
    def revision = revision()
    def registry = registry(branch, revision)

    docker.withRegistry(registry['uri'], { ->
        sh registry['login']
        docker.build(registry['image']).push(registry['tag'])
    })

    stage 'Bundle'
    sh sprintf('sed -i -e %s -e %s -e %s -e %s appspec.yml scripts/codedeploy/*', [
        "s/\\\${CODEDEPLOY_USER}/${env.CODEDEPLOY_USER}/g",
        "s/^ECR_REPOSITORY_URI=.*/ECR_REPOSITORY_URI=${env.ECR_REPOSITORY_URI}/",
        "s/^GIT_COMMIT=.*/GIT_COMMIT=${revision}/",
        "s/^AWS_REGION=.*/AWS_REGION=${env.AWS_DEFAULT_REGION}/",
    ])
    sh "tar -cvzf brian-${revision}.tar.gz appspec.yml scripts/codedeploy"
    sh "aws s3 cp brian-${revision}.tar.gz s3://${env.S3_REVISIONS_BUCKET}/brian-${revision}.tar.gz"

    if (branch != 'develop') return

    stage 'Deploy'
    sh sprintf('aws deploy create-deployment %s %s %s,bundleType=tgz,key=%s', [
        '--application-name brian',
        "--deployment-group-name ${env.CODEDEPLOY_PUBLISHING_DEPLOYMENT_GROUP}",
        "--s3-location bucket=${env.S3_REVISIONS_BUCKET}",
        "brian-${revision}.tar.gz",
    ])
}

def registry(branch, tag) {
    [
        hub: [
            login: 'docker login --username=$DOCKERHUB_USER --password=$DOCKERHUB_PASS',
            image: "${env.DOCKERHUB_REPOSITORY}/brian",
            tag: 'live',
            uri: "https://${env.DOCKERHUB_REPOSITORY_URI}",
        ],
        ecr: [
            login: '$(aws ecr get-login)',
            image: 'brian',
            tag: tag,
            uri: "https://${env.ECR_REPOSITORY_URI}",
        ],
    ][branch == 'live' ? 'hub' : 'ecr']
}

def revision() {
    def matcher = (readFile('git-tag').trim() =~ /^release\/(\d+\.\d+\.\d+(?:-rc\d+)?)$/)
    matcher.matches() ? matcher[0][1] : readFile('git-commit').trim()
}
