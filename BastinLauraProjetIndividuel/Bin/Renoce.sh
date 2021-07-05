cd ..
RENOCEDIR=$PWD
cd Bin
sudo chmod a+rwx /home/
export _JAVA_OPTIONS='-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel -Dswing.crossplatformlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel'
java -Xmx6000m -Djava.library.path=$RENOCEDIR/Legacy -jar Renoce.jar

