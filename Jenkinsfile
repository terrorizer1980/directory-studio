/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 8, unit: 'HOURS')
  }
  triggers {
    cron('@weekly')
    pollSCM('@daily')
  }
  stages {
    stage ('Debug') {
      options {
        timeout(time: 1, unit: 'HOURS')
        retry(2)
      }
      agent {
        label 'ubuntu && !H28 && !H36 && !H40'
      }
      steps {
        script {
          docker.image('osixia/openldap:1.3.0').withRun() { openldap ->
            docker.image('389ds/dirsrv').withRun('-e DS_DM_PASSWORD=admin', 'bash -c "set -m; /usr/lib/dirsrv/dscontainer -r & while ! /usr/lib/dirsrv/dscontainer -H; do sleep 5; done; sleep 5; /usr/sbin/dsconf localhost backend create --suffix dc=example,dc=org --be-name example; fg"') { fedora389ds ->
              docker.image('apachedirectory/maven-build:jdk-8').inside("--link=${openldap.id}:openldap -e OPENLDAP_HOST=openldap -e OPENLDAP_PORT=389 --link=${fedora389ds.id}:fedora389ds -e FEDORA_389DS_HOST=fedora389ds -e FEDORA_389DS_PORT=3389") {
                sh 'export DISPLAY=:99; env; ps aux'
              }
            }
          }
        }
      }
      post {
        always {
          deleteDir()
        }
      }
    }
    stage ('Build and Test') {
      parallel {
        stage ('Linux Java 11') {
          options {
            timeout(time: 4, unit: 'HOURS')
            retry(2)
          }
          agent {
            label 'ubuntu && !H28 && !H36 && !H40'
          }
          steps {
            script {
              docker.image('osixia/openldap:1.3.0').withRun() { openldap ->
                docker.image('389ds/dirsrv').withRun('-e DS_DM_PASSWORD=admin', 'bash -c "set -m; /usr/lib/dirsrv/dscontainer -r & while ! /usr/lib/dirsrv/dscontainer -H; do sleep 5; done; sleep 5; /usr/sbin/dsconf localhost backend create --suffix dc=example,dc=org --be-name example; fg"') { fedora389ds ->
                  docker.image('apachedirectory/maven-build:jdk-11').inside("--link=${openldap.id}:openldap -e OPENLDAP_HOST=openldap -e OPENLDAP_PORT=389 --link=${fedora389ds.id}:fedora389ds -e FEDORA_389DS_HOST=fedora389ds -e FEDORA_389DS_PORT=3389") {
                    sh 'export DISPLAY=:99; mvn -V -U -f pom-first.xml clean install && mvn -V clean install -Dorg.eclipse.swtbot.search.timeout=20000 -Denable-ui-tests'
                  }
                }
              }
            }
          }
          post {
            always {
              junit '**/target/surefire-reports/*.xml'
              archiveArtifacts artifacts:'product/target/products/*.zip,product/target/products/*.tar.gz,tests/test.integration.ui/screenshots/*', allowEmptyArchive:true
              deleteDir()
            }
          }
        }
        stage ('Linux Java 15') {
          options {
            timeout(time: 4, unit: 'HOURS')
            retry(2)
          }
          agent {
            label 'ubuntu && !H28 && !H36 && !H40'
          }
          steps {
            script {
              docker.image('osixia/openldap:1.3.0').withRun() { openldap ->
                docker.image('389ds/dirsrv').withRun('-e DS_DM_PASSWORD=admin', 'bash -c "set -m; /usr/lib/dirsrv/dscontainer -r & while ! /usr/lib/dirsrv/dscontainer -H; do sleep 5; done; sleep 5; /usr/sbin/dsconf localhost backend create --suffix dc=example,dc=org --be-name example; fg"') { fedora389ds ->
                  docker.image('apachedirectory/maven-build:jdk-15').inside("--link=${openldap.id}:openldap -e OPENLDAP_HOST=openldap -e OPENLDAP_PORT=389 --link=${fedora389ds.id}:fedora389ds -e FEDORA_389DS_HOST=fedora389ds -e FEDORA_389DS_PORT=3389") {
                    sh 'export DISPLAY=:99; mvn -V -U -f pom-first.xml clean install && mvn -V clean install -Dorg.eclipse.swtbot.search.timeout=20000 -Denable-ui-tests'
                  }
                }
              }
            }
          }
          post {
            always {
              junit '**/target/surefire-reports/*.xml'
              archiveArtifacts artifacts:'tests/test.integration.ui/screenshots/*', allowEmptyArchive:true
              deleteDir()
            }
          }
        }
        stage ('Windows Java 11') {
          options {
            timeout(time: 4, unit: 'HOURS')
            retry(2)
          }
          agent {
            label 'Windows'
          }
          steps {
            bat '''
            set JAVA_HOME=F:\\jenkins\\tools\\java\\latest11
            set MAVEN_OPTS="-Xmx512m"
            call F:\\jenkins\\tools\\maven\\latest3\\bin\\mvn -V -U -f pom-first.xml clean install
            call F:\\jenkins\\tools\\maven\\latest3\\bin\\mvn -V clean install -Dorg.eclipse.swtbot.search.timeout=20000 -Denable-ui-tests
            '''
          }
          post {
            always {
              junit '**/target/surefire-reports/*.xml'
              archiveArtifacts artifacts:'tests/test.integration.ui/screenshots/*', allowEmptyArchive:true
              deleteDir()
            }
          }
        }
      }
    }
  }
  post {
    failure {
      mail to: 'notifications@directory.apache.org',
      subject: "Jenkins pipeline failed: ${currentBuild.fullDisplayName}",
      body: "Jenkins build URL: ${env.BUILD_URL}"
    }
    fixed {
      mail to: 'notifications@directory.apache.org',
      subject: "Jenkins pipeline fixed: ${currentBuild.fullDisplayName}",
      body: "Jenkins build URL: ${env.BUILD_URL}"
    }
  }
}

