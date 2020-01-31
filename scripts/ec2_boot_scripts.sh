#!/bin/bash -x
REGION=$(curl 169.254.169.254/latest/meta-data/placement/availability-zone/ | sed 's/[a-z]$//')
sudo yum update -y
sudo yum install ruby -y
sudo yum install wget -y

# Install code deploy agent
cd /home/ec2_user
wget https://aws-codedeploy-$REGION.s3.amazonaws.com/latest/install
chmod +x ./install
sudo ./install auto

# Install JDK 11
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/11.0.2+9/f51449fcd52f4d52b93a989c5c56ed3c/jdk-11.0.2_linux-x64_bin.rpm
rpm -Uvh jdk-11.0.2_linux-x64_bin.rpm
sudo rpm -Uvh jdk-11.0.2_linux-x64_bin.rpm

# Status of code deploy agent
sudo service codedeploy-agent status

# Create winwin service to run the server in daemon mode
sudo touch /etc/init.d/winwin-service
sudo chmod +x /etc/init.d/winwin-service
# Copy the code from winwin-service into /etc/init.d/winwin-service 