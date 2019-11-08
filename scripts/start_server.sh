sudo kill $(pgrep java)
echo "Starting WinWin application"
if [[ "$WINWIN_ENV" = "Development" ]]; then
	nohup sudo java -Xmx768m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Staging" ]]; then
	nohup sudo java -Xmx1792m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
elif [[ "$WINWIN_ENV" = "Production" ]]; then
	nohup sudo java -Xmx3072m -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
fi
echo "WinWin application started"