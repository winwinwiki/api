chmod 777 /home/ec2-user/winwin-0.0.1-SNAPSHOT.jar

if [[ "$WINWIN_ENV" = "Development" ]]; then
	echo "Starting WinWin application for environment: $WINWIN_ENV"
	nohup sudo java -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
	echo "WinWin application started for environment: $WINWIN_ENV"
elif [[ "$WINWIN_ENV" = "Staging" ]]; then
	nohup sudo java -Xmx1792m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Production" ]]; then
	nohup sudo java -Xmx3072m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
fi