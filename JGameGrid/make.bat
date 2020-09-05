
set now=%time%


call mdist

cd apludist
dirdate -R -ALL time=$$:$$:$$ date=$$:$$:$$$$ *.* >nul

cd ..

call mzip


time %now%
echo Zeit zurueckgestellt auf 
time /t

