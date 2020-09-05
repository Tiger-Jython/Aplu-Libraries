call mdist

cd apludist
dirdate -R -ALL time=$$:$$:$$ date=$$:$$:$$$$ *.*

cd ..

call mzip

time %now%
echo Zeit zurueckgestellt auf 
time /t

