#service winwin-service start
sudo kill $(pgrep java)
echo "Starting WinWin application"
nohup sudo java -Dserver.port=80 -jar winwin-0.0.1-SNAPSHOT.jar /tmp 2>> /dev/null >> /dev/null &
echo "WinWin application started"