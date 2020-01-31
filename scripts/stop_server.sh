if pgrep -x "java" >/dev/null
then
    echo "Stopping WinWin application"
	sudo kill $(pgrep java)
	echo "WinWin application Stopped"
fi