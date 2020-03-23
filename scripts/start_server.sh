cd /home/ec2-user/
sudo chmod +x winwin-0.0.1-SNAPSHOT.jar
sudo chmod +x elastic-apm-agent-1.12.0.jar
echo "Starting NewImpact application for environment: $WINWIN_ENV"

if [[ "$WINWIN_ENV" = "Development" ]]; then
        nohup sudo java -Xmx768m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Staging" ]]; then
        nohup sudo java -Xmx3072m -Dserver.port=80 -javaagent:/home/ec2-user/elastic-apm-agent-1.12.0.jar -Delastic.apm.service_name=winwin-service-stage -Delastic.apm.application_packages=com.winwin.winwin -Delastic.apm.server_urls=http://wiki-stage.newimpact.care:8200 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
else
        nohup sudo java -Xmx4096m -Dserver.port=80 -javaagent:/home/ec2-user/elastic-apm-agent-1.12.0.jar -Delastic.apm.service_name=winwin-service-production -Delastic.apm.application_packages=com.winwin.winwin -Delastic.apm.server_urls=http://wiki.newimpact.care:8200 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
fi
echo "NewImpact application started for environment: $WINWIN_ENV"