FROM jenkins/jenkins:2.267-jdk11 AS jenkins_prod

USER root

ENV CASC_JENKINS_CONFIG /home/jenkins/casc.yaml
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_HOME /home/jenkins
ENV COPY_REFERENCE_FILE_LOG /home/jenkins/copy_reference_file.log

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt 
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

WORKDIR /home/jenkins


FROM jenkins_prod AS jenkins_local

RUN echo "    StrictHostKeyChecking no" >> /etc/ssh/ssh_config

ARG DOCKER_VERSION=19.03.13

RUN cd /tmp/ && \
    curl -sSL -O https://download.docker.com/linux/static/stable/x86_64/docker-$DOCKER_VERSION.tgz && \
    tar zxf docker-$DOCKER_VERSION.tgz && \
    mkdir -p /usr/local/bin && \
    mv ./docker/docker /usr/local/bin && \
    chmod +x /usr/local/bin/docker && \
    rm -rf /tmp/*