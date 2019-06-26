#service winwin-service stop
echo "Stopping WinWin application"
sudo kill $(pgrep java)
echo "WinWin application Stopped"