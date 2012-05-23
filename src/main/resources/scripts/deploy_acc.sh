JETTY_HOME=/var/local/jetty-acc/
SOURCE=/tmp/deploy/acc
TARGET=$JETTY_HOME/webapps/root.war


function copyWar(){
	if [ -d $SOURCE ] 
		then 
 			if [ "$(ls -A $SOURCE)" ] 
				then
    			echo "*** Moving war to $TARGET";
					mv $SOURCE/*.war $TARGET;
					touch $JETTY_HOME/contexts/root.xml;
			else
				echo "Deploy directory is empty";
  		fi
	else
		echo "Deploy directory does not exist";
	fi
}


# Execute
copyWar;


