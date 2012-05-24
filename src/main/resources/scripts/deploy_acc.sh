JETTY_HOME=/var/local/jetty-acc
SOURCE=/tmp/deploy/acc/target/
TARGET=/var/local/jetty-acc/webapps/root.war

function copyWar() {
	if [ -d $SOURCE ] 
		then 
 			if [ "$(ls -A $SOURCE)" ] 
				then
    			echo "*** Moving war to $TARGET";
    			if [ -f /var/run/jetty-acc.pid ]
    			 then
    			   echo "Killing pid `cat /var/run/jetty-acc.pid`";
    			   cat /var/run/jetty-acc.pid | xargs kill -9;
    			 fi
					mv $SOURCE/*.war $TARGET;
					/etc/init.d/jetty-acc start;
			else
				echo "*** Deploy directory is empty";
  		fi
	else
		echo "*** Deploy directory does not exist";
	fi
}

# Execute
copyWar;


