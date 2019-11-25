if pgrep -x "java" >/dev/null
then
    echo "Stopping WinWin application"
	sudo kill $(pgrep java)
fi

chmod +x /home/ec2-user/winwin-0.0.1-SNAPSHOT.jar
echo "Starting WinWin application for environment:  $WINWIN_ENV"
if [[ "$WINWIN_ENV" = "Development" ]]; then
	nohup sudo java -Xmx768m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Staging" ]]; then
	nohup sudo java -Xmx1792m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Production" ]]; then
	nohup sudo java -Xmx3072m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
fi
echo "WinWin application started for environment:  $WINWIN_ENV"