
if [ -d "bin" ] ; then
	rm -r bin
fi

mkdir bin

cd ../plugins

mvn package

cp target/plugins-1.0-SNAPSHOT.jar ../deploy/bin

cd ../service

mvn package -Dmaven.test.skip

cp target/analytics-service-1.0-SNAPSHOT.jar ../deploy/bin

cd ../studio

activator dist

cp target/universal/studio-1.0.zip ../deploy/bin

cd ../utilities/Wrapper

mvn package

cp target/Wrapper-1.0.jar ../../deploy/bin


