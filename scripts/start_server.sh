cd /home/ec2-user/
chmod +x winwin-0.0.1-SNAPSHOT.jar
chmod +x elastic-apm-agent-1.12.0.jar
echo "Starting WinWin application for environment: $WINWIN_ENV"
if [[ "$WINWIN_ENV" = "Development" ]]; then
	nohup sudo java -Xmx768m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Staging" ]]; then
	nohup sudo java -Xmx3072m -Dserver.port=80 -javaagent:/home/ec2-user/elastic-apm-agent-1.12.0.jar -Delastic.apm.service_name=winwin-service-stage -Delastic.apm.application_packages=com.winwin.winwin -Delastic.apm.server_urls=http://wiki-stage.winwin.care:8200  -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Production" ]]; then
	nohup sudo java -Xmx4096m -Dserver.port=80 -javaagent:/home/ec2-user/elastic-apm-agent-1.12.0.jar -Delastic.apm.service_name=winwin-service-stage -Delastic.apm.application_packages=com.winwin.winwin -Delastic.apm.server_urls=http://wiki.winwin.care:8200  -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
fi
echo "WinWin application started for environment: $WINWIN_ENV"