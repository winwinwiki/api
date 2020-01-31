FILE=/home/ec2-user/winwin-0.0.1-SNAPSHOT.jar
if test -f "$FILE" >/dev/null
then
	echo "Creating backup of application jar"
	mv /home/ec2-user/winwin-0.0.1-SNAPSHOT.jar /home/ec2-user/winwin-0.0.1-SNAPSHOT.jar.bkp
	echo "WinWin application Stopped"
fi