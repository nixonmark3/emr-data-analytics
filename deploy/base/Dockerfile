## Base docker image for Analytics apps 

FROM ubuntu:latest

MAINTAINER PSS Applied Research

ENV DEBIAN_FRONTEND noninteractive

# Install prerequisites
RUN apt-get update
RUN apt-get install -y software-properties-common

# Install java8
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# Define JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Install dependencies, python 2 & 3, and a select set of libraries 
RUN apt-get install -y \
    build-essential \
    gcc \
    make \
    wget \
    curl \
    tmux \
    git \
    libxml2-dev \
    libsqlite3-dev \
    sqlite3 \
    bzip2 \
    libbz2-dev \
    libfreetype6 \
    libfreetype6-dev \
    libpng-dev \
    libzmq3-dev \
    python-software-properties \
    software-properties-common \
    python-pip \
    python3-software-properties \
    python3-pip \
    python3-requests \
    python3-simplejson \
    python3-imaging \
    python3-zmq \
    python3-numpy \
    python3-pandas \
    python3-scipy \
    python3-matplotlib && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install additional python packages 
RUN pip3 install pymongo sklearn
RUN pip install kafka-python
